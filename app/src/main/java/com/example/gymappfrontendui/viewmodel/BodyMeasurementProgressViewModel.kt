package com.example.gymappfrontendui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymappfrontendui.db.entity.BodyMeasurement
import com.example.gymappfrontendui.repository.BodyMeasurementRepository
import com.example.gymappfrontendui.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

enum class MeasurementType(val displayName: String) {
    WEIGHT("Weight (kg)"),
    WAIST("Waist (cm)"),
    CHEST("Chest (cm)"),
    ARM("Arm (cm)"),
    FOREARM("Forearm (cm)"),
    THIGH("Thigh (cm)"),
    CALF("Calf (cm)"),
    HIPS("Hips (cm)")
}
data class BodyMeasurementProgressState(
    val measurementTypes: List<MeasurementType> = MeasurementType.entries,
    val selectedMeasurementType: MeasurementType = MeasurementType.WEIGHT,
    val chartValues: List<Double> = emptyList(),
    val chartXAxisLabels: List<String> = emptyList(),
    val currentFilter: DateRangeFilter = DateRangeFilter.MONTH_3,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val latestMeasurement: String = "N/A",
    val isLoading: Boolean = true,
    val error: String? = null
)

class BodyMeasurementProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BodyMeasurementRepository(application.applicationContext)
    private val userRepository = UserRepository(application.applicationContext)

    private val _state = MutableStateFlow(BodyMeasurementProgressState())
    val state: StateFlow<BodyMeasurementProgressState> = _state.asStateFlow()

    private var currentUserId: Int? = null
    private val _allMeasurements = MutableStateFlow<List<BodyMeasurement>>(emptyList())

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            currentUserId = userRepository.getLoggedInUserId() ?: userRepository.getGuestUserId()
            if (currentUserId == null) {
                _state.update { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }
            loadAllMeasurements()
        }
    }

    private fun loadAllMeasurements() {
        viewModelScope.launch {
            repository.getAvailableBodyMeasurements()
                .map { measurements ->
                    measurements.filter { it.userId == currentUserId }
                }
                .distinctUntilChanged()
                .collectLatest { userMeasurements ->
                    _allMeasurements.value = userMeasurements.sortedBy { parseDate(it.date) }
                    processChartData()
                }
        }
    }

    fun selectMeasurementType(type: MeasurementType) {
        if (type == _state.value.selectedMeasurementType) return
        _state.update { it.copy(selectedMeasurementType = type) }
        processChartData()
    }
    fun setDateFilter(filter: DateRangeFilter) {
        if (filter == _state.value.currentFilter) return

        if (filter == DateRangeFilter.CUSTOM) {
            _state.update { it.copy(currentFilter = filter) }
            if (_state.value.customStartDate != null && _state.value.customEndDate != null) {
                processChartData()
            }
        } else {
            _state.update {
                it.copy(
                    currentFilter = filter,
                    isLoading = true,
                    customStartDate = null,
                    customEndDate = null
                )
            }
            processChartData()
        }
    }

    fun selectCustomStartDate(date: LocalDate) {
        _state.update { it.copy(customStartDate = date) }
        if (_state.value.customEndDate != null) {
            processChartData()
        }
    }

    fun selectCustomEndDate(date: LocalDate) {
        _state.update { it.copy(customEndDate = date) }
        if (_state.value.customStartDate != null) {
            processChartData()
        }
    }

    private fun processChartData() {
        viewModelScope.launch {
            val filter = _state.value.currentFilter

            if (filter == DateRangeFilter.CUSTOM && (_state.value.customStartDate == null || _state.value.customEndDate == null)) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        chartValues = emptyList(),
                        chartXAxisLabels = emptyList()
                    )
                }
                return@launch
            }

            _state.update { it.copy(isLoading = true) }
            val allMeasurements = _allMeasurements.value
            val type = _state.value.selectedMeasurementType

            if (allMeasurements.isEmpty()) {
                _state.update {
                    it.copy(
                        chartValues = emptyList(),
                        chartXAxisLabels = emptyList(),
                        latestMeasurement = "N/A",
                        isLoading = false
                    )
                }
                return@launch
            }

            val filteredMeasurements = applyDateFilter(allMeasurements, filter)

            if (filteredMeasurements.isEmpty()) {
                _state.update {
                    it.copy(
                        chartValues = emptyList(),
                        chartXAxisLabels = emptyList(),
                        isLoading = false
                    )
                }
                return@launch
            }

            val (values, labels) = calculateChartData(filteredMeasurements, type)
            val latest = calculateLatestMeasurement(allMeasurements, type)

            _state.update {
                it.copy(
                    chartValues = values,
                    chartXAxisLabels = labels,
                    latestMeasurement = latest,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private fun parseDate(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) {
            Log.w("BodyMeasureProgressVM", "Attempted to parse null or blank date string.")
            return null
        }
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            _state.update { it.copy(error = "Unrecognized date format in history: $dateString") }
            null
        } catch (e: Exception) {
            _state.update { it.copy(error = "Error processing date: $dateString") }
            null
        }
    }

    private fun applyDateFilter(
        measurements: List<BodyMeasurement>,
        filter: DateRangeFilter
    ): List<BodyMeasurement> {

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
            return measurements
        }

        return measurements.mapNotNull { measurement ->
            val date = parseDate(measurement.date)
            if (date != null && date >= startDate && date <= endDate) {
                measurement
            } else {
                null
            }
        }
    }

    private fun getMeasurementValue(measurement: BodyMeasurement, type: MeasurementType): Float? {
        val value = when (type) {
            MeasurementType.WEIGHT -> measurement.weight
            MeasurementType.WAIST -> measurement.waist
            MeasurementType.CHEST -> measurement.chest
            MeasurementType.ARM -> measurement.arm
            MeasurementType.FOREARM -> measurement.forearm
            MeasurementType.THIGH -> measurement.thigh
            MeasurementType.CALF -> measurement.calf
            MeasurementType.HIPS -> measurement.hips
        }
        return if (value != null && value > 0f) value else null
    }

    private fun calculateChartData(
        measurements: List<BodyMeasurement>,
        type: MeasurementType
    ): Pair<List<Double>, List<String>> {
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())

        val sortedData = measurements.mapNotNull { measurement ->
            val date = parseDate(measurement.date)
            val value = getMeasurementValue(measurement, type)
            if (date != null && value != null) {
                date to value
            } else {
                null
            }
        }

        val values = sortedData.map { it.second.toDouble() }
        val labels = sortedData.map { it.first.format(dateFormatter) }

        return Pair(values, labels)
    }

    private fun calculateLatestMeasurement(
        measurements: List<BodyMeasurement>,
        type: MeasurementType
    ): String {
        val latestEntry = measurements
            .mapNotNull { m ->
                val date = parseDate(m.date)
                val value = getMeasurementValue(m, type)
                if (date != null && value != null) {
                    Triple(m, date, value)
                } else {
                    null
                }
            }
            .maxByOrNull { it.second }

        return if (latestEntry != null) {
            val (_, date, value) = latestEntry
            val unit = if (type == MeasurementType.WEIGHT) "kg" else "cm"
            val dateStr = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            "$value $unit on $dateStr"
        } else {
            "N/A"
        }
    }
}