package com.example.gymappfrontendui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymappfrontendui.db.dto.WorkoutSetWithDate
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.repository.SetRepository
import com.example.gymappfrontendui.repository.ExerciseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.max

enum class DateRangeFilter {
    MONTH_1, MONTH_3, MONTH_6, YEAR_1, ALL_TIME, CUSTOM
}

enum class ChartMetric(val displayName: String) {
    VOLUME("Max Volume"),
    MAX_WEIGHT("Max Weight (kg)")
}

data class WorkoutProgressState(
    val exercises: List<Exercise> = emptyList(),
    val selectedExerciseId: Int? = null,
    val selectedExerciseName: String = "Select Exercise",
    val chartValues: List<Double> = emptyList(),
    val chartXAxisLabels: List<String> = emptyList(),
    val currentFilter: DateRangeFilter = DateRangeFilter.MONTH_3,
    val personalRecord: String = "N/A",
    val isLoading: Boolean = true,
    val error: String? = null,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val chartMetrics: List<ChartMetric> = ChartMetric.entries,
    val selectedChartMetric: ChartMetric = ChartMetric.VOLUME
)

class WorkoutProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val exerciseRepository = ExerciseRepository(application.applicationContext)
    private val setRepository = SetRepository(application.applicationContext)

    private val _state = MutableStateFlow(WorkoutProgressState())
    val state: StateFlow<WorkoutProgressState> = _state.asStateFlow()

    private val dateFormatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    )

    init {
        viewModelScope.launch {
            exerciseRepository.getAvailableExercises().collectLatest { fetchedExercises ->
                val currentSelectedId = _state.value.selectedExerciseId
                val updatedExercises = fetchedExercises.sortedBy { it.name }
                _state.update { it.copy(exercises = updatedExercises, isLoading = false) }

                if (updatedExercises.isNotEmpty()) {
                    if (currentSelectedId == null || updatedExercises.none { it.exerciseId == currentSelectedId }) {
                        selectExercise(updatedExercises.first().exerciseId)
                    } else {
                        loadWorkoutDataForSelectedExercise()
                    }
                } else {
                    _state.update { WorkoutProgressState(isLoading = false) }
                }
            }
        }
    }

    fun selectExercise(exerciseId: Int?) {
        if (exerciseId == null || exerciseId == _state.value.selectedExerciseId) return
        _state.update { currentState ->
            val selectedExercise = currentState.exercises.find { it.exerciseId == exerciseId }
            currentState.copy(
                selectedExerciseId = exerciseId,
                selectedExerciseName = selectedExercise?.name ?: "Unknown",
                isLoading = true,
                chartValues = emptyList(),
                chartXAxisLabels = emptyList(),
                personalRecord = "N/A",
                error = null
            )
        }
        loadWorkoutDataForSelectedExercise()
    }

    fun setDateFilter(filter: DateRangeFilter) {
        if (filter == _state.value.currentFilter) return

        if (filter == DateRangeFilter.CUSTOM) {
            _state.update { it.copy(currentFilter = filter) }
            if (_state.value.customStartDate != null && _state.value.customEndDate != null) {
                loadWorkoutDataForSelectedExercise()
            }
        } else {
            _state.update {
                it.copy(
                    currentFilter = filter,
                    isLoading = true,
                    error = null,
                    customStartDate = null,
                    customEndDate = null
                )
            }
            loadWorkoutDataForSelectedExercise()
        }
    }

    fun selectCustomStartDate(date: LocalDate) {
        _state.update { it.copy(customStartDate = date) }
        if (_state.value.customEndDate != null) {
            loadWorkoutDataForSelectedExercise()
        }
    }

    fun selectCustomEndDate(date: LocalDate) {
        _state.update { it.copy(customEndDate = date) }
        if (_state.value.customStartDate != null) {
            loadWorkoutDataForSelectedExercise()
        }
    }

    fun selectChartMetric(metric: ChartMetric) {
        if (metric == _state.value.selectedChartMetric) return
        _state.update { it.copy(selectedChartMetric = metric, isLoading = true) }
        loadWorkoutDataForSelectedExercise()
    }

    private fun loadWorkoutDataForSelectedExercise() {
        val exerciseId = _state.value.selectedExerciseId ?: return
        val filter = _state.value.currentFilter

        if (filter == DateRangeFilter.CUSTOM && (_state.value.customStartDate == null || _state.value.customEndDate == null)) {
            _state.update {
                it.copy(
                    isLoading = false,
                    chartValues = emptyList(),
                    chartXAxisLabels = emptyList()
                )
            }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                setRepository.getWorkoutSetsWithDateForExercise(exerciseId)
                    .collectLatest { setsWithDate: List<WorkoutSetWithDate> ->
                        if (setsWithDate.isEmpty()) {
                            _state.update {
                                it.copy(
                                    chartValues = emptyList(),
                                    chartXAxisLabels = emptyList(),
                                    personalRecord = "N/A",
                                    isLoading = false
                                )
                            }
                            return@collectLatest
                        }

                        val filteredSets = applyDateFilter(setsWithDate, filter)

                        if (filteredSets.isEmpty()) {
                            _state.update {
                                it.copy(
                                    chartValues = emptyList(),
                                    chartXAxisLabels = emptyList(),
                                    isLoading = false
                                )
                            }
                            return@collectLatest
                        }

                        val (values, labels) = calculateChartData(filteredSets)
                        val pr = calculatePersonalRecord(filteredSets)

                        _state.update {
                            it.copy(
                                chartValues = values,
                                chartXAxisLabels = labels,
                                personalRecord = pr,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load workout data: ${e.message}"
                    )
                }
            }
        }
    }

    private fun parseDate(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) {
            Log.w("WorkoutProgressVM", "Attempted to parse null or blank date string.")
            return null
        }

        return try {
            val timestampMillis = dateString.toLong()
            val instant = Instant.ofEpochMilli(timestampMillis)
            val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            localDate
        } catch (e: NumberFormatException) {
            val dateOnly = dateString.split(" ", "T").firstOrNull() ?: dateString
            for (formatter in dateFormatters) {
                try {
                    val parsedDate = LocalDate.parse(dateOnly, formatter)
                    return parsedDate
                } catch (e: DateTimeParseException) {
                }
            }
            _state.update { it.copy(error = "Unrecognized date format in history: $dateString") }
            null
        } catch (e: Exception) {
            _state.update { it.copy(error = "Error processing date: $dateString") }
            null
        }
    }

    private fun applyDateFilter(
        setsWithDate: List<WorkoutSetWithDate>,
        filter: DateRangeFilter
    ): List<WorkoutSetWithDate> {

        val today = LocalDate.now()
        val startDate: LocalDate = when (filter) {
            DateRangeFilter.MONTH_1 -> today.minusMonths(1)
            DateRangeFilter.MONTH_3 -> today.minusMonths(3)
            DateRangeFilter.MONTH_6 -> today.minusMonths(6)
            DateRangeFilter.YEAR_1 -> today.minusYears(1)
            DateRangeFilter.ALL_TIME -> LocalDate.MIN
            DateRangeFilter.CUSTOM -> _state.value.customStartDate ?: LocalDate.MIN
        }

        val endDate: LocalDate = when (filter) {
            DateRangeFilter.CUSTOM -> _state.value.customEndDate ?: today
            else -> today
        }

        if (filter == DateRangeFilter.ALL_TIME) {
            return setsWithDate
        }

        return try {
            val filtered = setsWithDate.mapNotNull { setWithDate ->
                val date = parseDate(setWithDate.workoutDate)
                if (date != null && date >= startDate && date <= endDate) {
                    setWithDate
                } else {
                    if (date != null) {
                    }
                    null
                }
            }
            filtered
        } catch (e: Exception) {
            _state.update { it.copy(error = "Error filtering data: ${e.message}") }
            emptyList()
        }
    }

    private fun calculateChartData(
        setsWithDate: List<WorkoutSetWithDate>
    ): Pair<List<Double>, List<String>> {
        val metric = _state.value.selectedChartMetric
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
        val dataMap = mutableMapOf<LocalDate, Double>()

        when (metric) {
            ChartMetric.VOLUME -> {
                setsWithDate.forEach { setWithDate ->
                    try {
                        val date = parseDate(setWithDate.workoutDate)
                        if (date != null) {
                            val weight = setWithDate.workoutSet.weight ?: 0f
                            val reps = setWithDate.workoutSet.reps ?: 0
                            val volume = (weight * reps.toFloat()).toDouble()
                            dataMap[date] = max(dataMap[date] ?: 0.0, volume)
                        }
                    } catch (e: Exception) {
                        Log.e("WorkoutProgressVM", "Error calculating volume for set: ${setWithDate.workoutSet}", e)
                    }
                }
            }
            ChartMetric.MAX_WEIGHT -> {
                setsWithDate.forEach { setWithDate ->
                    try {
                        val date = parseDate(setWithDate.workoutDate)
                        if (date != null) {
                            val weight = (setWithDate.workoutSet.weight ?: 0f).toDouble()
                            if (weight > 0.0) {
                                dataMap[date] = max(dataMap[date] ?: 0.0, weight)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("WorkoutProgressVM", "Error calculating Max Weight for set: ${setWithDate.workoutSet}", e)
                    }
                }
            }
        }

        val sortedEntries = dataMap.entries.sortedBy { it.key }
        val values = sortedEntries.map { it.value }
        val labels = sortedEntries.map { it.key.format(dateFormatter) }

        return Pair(values, labels)
    }

    private fun calculatePersonalRecord(
        setsWithDate: List<WorkoutSetWithDate>
    ): String {
        val maxWeightSetWithDate = setsWithDate.maxByOrNull {
            it.workoutSet.weight ?: 0f
        }

        return if (maxWeightSetWithDate != null) {
            val weight = maxWeightSetWithDate.workoutSet.weight ?: 0f
            val reps = maxWeightSetWithDate.workoutSet.reps ?: 0

            if (weight == 0f) return "N/A"

            val weightStr = if (weight == weight.toInt().toFloat()) {
                weight.toInt().toString()
            } else {
                String.format(Locale.US, "%.1f", weight)
            }

            val dateStr = try {
                parseDate(maxWeightSetWithDate.workoutDate)?.format(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy")
                ) ?: ""
            } catch (e: Exception) {
                ""
            }

            "${weightStr} kg x ${reps} reps${if (dateStr.isNotEmpty()) " on $dateStr" else ""}"
        } else {
            "N/A"
        }
    }
}