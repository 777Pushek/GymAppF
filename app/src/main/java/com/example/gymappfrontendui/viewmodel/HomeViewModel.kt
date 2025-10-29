package com.example.gymappfrontendui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymappfrontendui.repository.BodyMeasurementRepository
import com.example.gymappfrontendui.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

data class HomeUiState(
    val lastWorkoutSummary: String = "No data",
    val lastWorkoutSubtitle: String = "Completed 0 exercises",
    val currentWeight: String = "No data"
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val workoutRepository = WorkoutRepository(application.applicationContext)
    private val bodyMeasurementRepository = BodyMeasurementRepository(application.applicationContext)

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())

    private fun parseDate(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) {
            Log.w("HomeViewModel", "Attempted to parse null or blank date string.")
            return null
        }

        return try {
            val timestampMillis = dateString.toLong()
            val instant = Instant.ofEpochMilli(timestampMillis)
            instant.atZone(ZoneId.systemDefault()).toLocalDate()
        } catch (e: NumberFormatException) {
            try {
                LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: DateTimeParseException) {
                Log.e("HomeViewModel", "Unrecognized date format: $dateString", e)
                null
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error processing date: $dateString", e)
            null
        }
    }
    private val lastWorkoutFlow = workoutRepository.getAvailableWorkoutsWithExercises()
        .map { workouts ->
            val latestWorkout = workouts
                .filter { parseDate(it.workout.date) != null }
                .sortedByDescending { parseDate(it.workout.date) }
                .firstOrNull()

            if (latestWorkout == null) {
                Pair("No data", "Completed 0 exercises")
            } else {
                val dateStr = parseDate(latestWorkout.workout.date)?.format(dateFormatter) ?: "Unknown Date"
                val exerciseCount = latestWorkout.workoutExercises.size
                Pair(dateStr, "Completed $exerciseCount exercises")
            }
        }

    private val currentWeightFlow = bodyMeasurementRepository.getAvailableBodyMeasurements()
        .map { measurements ->
            val latestMeasurement = measurements
                .filter { it.weight != null && it.weight > 0f && parseDate(it.date) != null }
                .sortedByDescending { parseDate(it.date) }
                .firstOrNull()

            if (latestMeasurement?.weight != null) {
                "${latestMeasurement.weight} kg"
            } else {
                "No data"
            }
        }

    val uiState: StateFlow<HomeUiState> = combine(
        lastWorkoutFlow,
        currentWeightFlow
    ) { (workoutSummary, workoutSubtitle), weight ->
        HomeUiState(
            lastWorkoutSummary = workoutSummary,
            lastWorkoutSubtitle = workoutSubtitle,
            currentWeight = weight
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}