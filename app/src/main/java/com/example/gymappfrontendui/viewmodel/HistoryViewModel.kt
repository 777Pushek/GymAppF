package com.example.gymappfrontendui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymappfrontendui.db.entity.Workout
import com.example.gymappfrontendui.models.ExerciseDetail
import com.example.gymappfrontendui.models.WorkoutHistoryItem
import com.example.gymappfrontendui.repository.ExerciseRepository
import com.example.gymappfrontendui.repository.SetRepository
import com.example.gymappfrontendui.repository.UserRepository
import com.example.gymappfrontendui.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.gymappfrontendui.db.entity.Set
import com.example.gymappfrontendui.db.entity.WorkoutExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.collections.flatten

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val workoutRepository = WorkoutRepository(app.applicationContext)
    private val userRepository = UserRepository(app.applicationContext)
    private val exerciseRepository = ExerciseRepository(app.applicationContext)
    private val setRepository = SetRepository(app.applicationContext)

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val userWorkoutsFlow: Flow<List<Workout>> = combine(
        workoutRepository.getAvailableWorkouts(),
        userRepository.getLoggedInUserIdFlow(),
        userRepository.getGuestUserIdFlow()
    ) { allWorkouts, userId, guestId ->
        val actualUserId = userId ?: guestId
        allWorkouts.filter { it.userId == actualUserId }
    }


    private val userWorkoutExercisesFlow: Flow<List<WorkoutExercise>> = combine(
        userWorkoutsFlow,
        workoutRepository.getAllWorkoutExercises()
    ) { userWorkouts, allWorkoutExercises ->
        val userWorkoutIds = userWorkouts
            .map { it.workoutId }
            .toSet()
        allWorkoutExercises.filter { it.workoutId in userWorkoutIds }
    }

    private val userSetsFlow: Flow<List<Set>> = userWorkoutExercisesFlow.flatMapLatest { userWorkoutExercises ->
        if (userWorkoutExercises.isEmpty()) {
            flowOf(emptyList<Set>())
        } else {
            val setFlows: List<Flow<List<Set>>> = userWorkoutExercises.map {
                setRepository.getSetByWorkoutExerciseId(it.workoutExerciseId)
            }
            combine(setFlows) { arrayOfSetLists ->
                arrayOfSetLists.toList().flatten()
            }
        }
    }

    private val allWorkoutsFlow: StateFlow<List<WorkoutHistoryItem>> = combine(
        userWorkoutsFlow,
        exerciseRepository.getAvailableExercises(),
        userWorkoutExercisesFlow,
        userSetsFlow
    ) { userWorkouts, allExercises, userWorkoutExercises, userSets ->

        val exercisesMap = allExercises.associateBy { it.exerciseId }
        val setsGroupedByWorkoutExerciseId = userSets.groupBy { it.workoutExerciseId }
        val workoutExercisesGroupedByWorkoutId = userWorkoutExercises.groupBy { it.workoutId }

        val validWorkoutIdsWithExercises = workoutExercisesGroupedByWorkoutId.keys

        userWorkouts.sortedByDescending { it.date.toLongOrNull() ?: 0L }
            .mapNotNull { workout ->
                if (workout.workoutId !in validWorkoutIdsWithExercises) {
                    return@mapNotNull null
                }
                var totalVolume = 0f
                val workoutExercisesForWorkout = workoutExercisesGroupedByWorkoutId[workout.workoutId] ?: emptyList()

                val exerciseDetails = workoutExercisesForWorkout.map { workoutExercise ->
                    val setsForExercise = setsGroupedByWorkoutExerciseId[workoutExercise.workoutExerciseId] ?: emptyList()
                    val bestSet = setsForExercise.maxWithOrNull(compareBy({ it.weight }, { it.reps }))
                    val bestSetString = bestSet?.let { "${it.weight} kg x ${it.reps}" } ?: "-"
                    setsForExercise.forEach { set ->
                        totalVolume += set.weight * set.reps
                    }
                    val exerciseName = exercisesMap[workoutExercise.exerciseId]?.name ?: "Unknown Exercise"

                    ExerciseDetail(
                        name = exerciseName,
                        setCount = setsForExercise.size,
                        bestSet = bestSetString
                    )
                }

                val timestamp = workout.date.toLongOrNull() ?: 0L
                if (timestamp == 0L) return@mapNotNull null
                val workoutDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()

                WorkoutHistoryItem(
                    workoutId = workout.workoutId,
                    workoutDate = workoutDate,
                    duration = formatDuration(workout.duration?.toLong() ?: 0L),
                    totalVolume = totalVolume,
                    exercises = exerciseDetails
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val historyState: StateFlow<List<WorkoutHistoryItem>> =
        combine(allWorkoutsFlow, _selectedDate) { all, selected ->
            if (selected == null) {
                all
            } else {
                all.filter { it.workoutDate == selected }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutDates: StateFlow<kotlin.collections.Set<LocalDate>> = allWorkoutsFlow.map { list ->
        list.map { it.workoutDate }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), kotlin.collections.emptySet())


    fun onDateSelected(date: LocalDate) {
        if (_selectedDate.value == date) {
            _selectedDate.value = null
        } else {
            _selectedDate.value = date
        }
    }

    fun clearDateFilter() {
        _selectedDate.value = null
    }

    fun deleteWorkout(workoutId: Int) {
        viewModelScope.launch {
            val workoutToDelete = workoutRepository.getWorkoutById(workoutId).firstOrNull()
            workoutToDelete?.let {
                workoutRepository.deleteWorkout(it)
            }
        }
    }

    private fun formatDuration(totalSeconds: Long): String {
        if (totalSeconds <= 0) return "00:00"
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}