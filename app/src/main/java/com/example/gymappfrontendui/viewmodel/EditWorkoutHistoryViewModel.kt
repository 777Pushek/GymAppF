package com.example.gymappfrontendui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.Set
import com.example.gymappfrontendui.db.entity.WorkoutExercise
import com.example.gymappfrontendui.models.ActiveWorkoutExercise
import com.example.gymappfrontendui.models.ActiveWorkoutSet
import com.example.gymappfrontendui.repository.ExerciseRepository
import com.example.gymappfrontendui.repository.SetRepository
import com.example.gymappfrontendui.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EditWorkoutHistoryState(
    val workoutId: Int,
    val date: String = "",
    val duration: String = "",
    val exercises: List<ActiveWorkoutExercise> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditWorkoutHistoryViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val workoutRepository = WorkoutRepository(app.applicationContext)
    private val setRepository = SetRepository(app.applicationContext)
    private val exerciseRepository = ExerciseRepository(app.applicationContext)
    private val workoutDao = AppDb.getInstance(app.applicationContext).workoutDao()

    private val workoutId: Int = checkNotNull(savedStateHandle["workoutId"])

    private val _editState = MutableStateFlow(EditWorkoutHistoryState(workoutId = workoutId))
    val editState: StateFlow<EditWorkoutHistoryState> = _editState.asStateFlow()

    private val removedSetIds = mutableSetOf<Int>()
    private val addedWorkoutExercises = mutableSetOf<ActiveWorkoutExercise>()

    init {
        loadWorkoutHistoryDetails()
    }

    private fun loadWorkoutHistoryDetails() {
        viewModelScope.launch {
            _editState.update { it.copy(isLoading = true, saveSuccess = false) }
            try {
                val workoutFlow = workoutRepository.getWorkoutById(workoutId)
                val workout = workoutFlow.firstOrNull()

                if (workout == null) {
                    _editState.update { it.copy(isLoading = false, error = "Workout not found") }
                    return@launch
                }

                val workoutExercisesWithSets = workoutDao.getExercisesWithSetsForWorkout(workoutId)
                val allExercisesMap = exerciseRepository.getAllExercisesMap()

                val activeExercises = workoutExercisesWithSets.mapNotNull { we ->
                    val exerciseEntity = allExercisesMap[we.workoutExercise.exerciseId]
                    if (exerciseEntity == null) {
                        Log.e("EditWorkoutHistoryVM", "Exercise with ID ${we.workoutExercise.exerciseId} not found!")
                        return@mapNotNull null
                    }

                    val activeSets = we.sets.map { setEntity ->
                        ActiveWorkoutSet(
                            setId = setEntity.setId,
                            workoutExerciseId = setEntity.workoutExerciseId,
                            setNumber = setEntity.position + 1,
                            weight = setEntity.weight.toString(),
                            reps = setEntity.reps.toString(),
                            isCompleted = true,
                            previousPerformance = ""
                        )
                    }.toMutableList()

                    ActiveWorkoutExercise(
                        exercise = exerciseEntity,
                        workoutExerciseId = we.workoutExercise.workoutExerciseId,
                        sets = activeSets
                    )
                }.toMutableList()

                _editState.update {
                    it.copy(
                        isLoading = false,
                        date = formatTimestamp(workout.date.toLongOrNull() ?: 0L),
                        duration = formatDuration(workout.duration?.toLong() ?: 0L),
                        exercises = activeExercises
                    )
                }

            } catch (e: Exception) {
                _editState.update { it.copy(isLoading = false, error = "Failed to load workout: ${e.message}") }
                Log.e("EditWorkoutHistoryVM", "Error loading workout", e)
            }
        }
    }

    fun addSetToExercise(exerciseId: Int) {
        val currentExercises = _editState.value.exercises.map { exercise ->
            exercise.copy(sets = exercise.sets.map { it.copy() }.toMutableList())
        }.toMutableList()

        currentExercises.find { it.exercise.exerciseId == exerciseId }?.let { workoutExercise ->
            val nextSetNumber = workoutExercise.sets.size + 1
            val newSet = ActiveWorkoutSet(
                setId = 0,
                workoutExerciseId = workoutExercise.workoutExerciseId,
                setNumber = nextSetNumber
            )
            workoutExercise.sets.add(newSet)
            _editState.update { it.copy(exercises = currentExercises) }
        }
    }


    fun removeSetFromExercise(exerciseId: Int, set: ActiveWorkoutSet) {
        val currentExercises = _editState.value.exercises.map { exercise ->
            exercise.copy(sets = exercise.sets.map { it.copy() }.toMutableList())
        }.toMutableList()

        currentExercises.find { it.exercise.exerciseId == exerciseId }?.let { workoutExercise ->
            val removed = workoutExercise.sets.removeIf { (it.setId != 0 && it.setId == set.setId) || (it.setId == 0 && it === set) }

            if (removed) {
                if (set.setId != 0) {
                    removedSetIds.add(set.setId)
                }
                workoutExercise.sets.forEachIndexed { index, currentSet ->
                    currentSet.setNumber = index + 1
                }
                _editState.update { it.copy(exercises = currentExercises) }
            }
        }
    }

    fun addExercisesToHistory(selectedExercises: List<Exercise>) {
        val currentExercises = _editState.value.exercises.map { it.copy(sets = it.sets.map { s-> s.copy() }.toMutableList()) }.toMutableList()
        var exercisesChanged = false

        selectedExercises.forEach { exercise ->
            if (currentExercises.none { it.exercise.exerciseId == exercise.exerciseId }) {

                val newActiveExercise = ActiveWorkoutExercise(
                    exercise = exercise,
                    workoutExerciseId = 0,
                    sets = mutableListOf(ActiveWorkoutSet(setNumber = 1, workoutExerciseId = 0))
                )
                currentExercises.add(newActiveExercise)
                addedWorkoutExercises.add(newActiveExercise)
                exercisesChanged = true
            }
        }
        if(exercisesChanged) {
            _editState.update { it.copy(exercises = currentExercises) }
        }
    }
    fun saveChanges() {
        viewModelScope.launch {
            val state = _editState.value
            if (state.isLoading || state.error != null) return@launch
            _editState.update { it.copy(error = null) }

            try {
                removedSetIds.forEach { setIdToDelete ->
                    val setToDelete = Set(setId = setIdToDelete, workoutExerciseId = 0, reps = 0, weight = 0f, position = 0)
                    setRepository.deleteSet(setToDelete)
                }
                removedSetIds.clear()

                state.exercises.forEachIndexed { exerciseIndex, activeExercise ->
                    var currentWorkoutExerciseId = activeExercise.workoutExerciseId

                    if (currentWorkoutExerciseId == 0 && addedWorkoutExercises.contains(activeExercise)) {
                        val newWorkoutExerciseEntity = WorkoutExercise(
                            workoutId = state.workoutId,
                            exerciseId = activeExercise.exercise.exerciseId,
                            position = exerciseIndex
                        )
                        currentWorkoutExerciseId = workoutRepository.insertWorkoutExercise(newWorkoutExerciseEntity).toInt()
                        Log.d("EditWorkoutHistoryVM", "Inserted new WorkoutExercise with ID: $currentWorkoutExerciseId")
                    } else if (currentWorkoutExerciseId != 0) {
                        // workoutRepository.updateWorkoutExercisePosition(currentWorkoutExerciseId, exerciseIndex)
                    } else {
                        Log.e("EditWorkoutHistoryVM", "Skipping sets for exercise without valid workoutExerciseId: ${activeExercise.exercise.name}")
                        return@forEachIndexed
                    }

                    activeExercise.sets.forEachIndexed { setIndex, activeSet ->
                        val weightValue = activeSet.weight.toFloatOrNull() ?: 0f
                        val repsValue = activeSet.reps.toIntOrNull() ?: 0

                        val setEntity = Set(
                            setId = if (activeSet.setId == 0) 0 else activeSet.setId,
                            workoutExerciseId = currentWorkoutExerciseId,
                            reps = repsValue,
                            weight = weightValue,
                            position = setIndex
                        )

                        if (activeSet.setId == 0) {
                            setRepository.insertSet(setEntity)
                        } else {
                            setRepository.updateSet(setEntity)
                        }
                    }
                }
                addedWorkoutExercises.clear()

                Log.d("EditWorkoutHistoryVM", "Changes saved successfully!")
                _editState.update { it.copy(saveSuccess = true) }

            } catch (e: Exception) {
                _editState.update { it.copy(error = "Failed to save changes: ${e.message}") }
                Log.e("EditWorkoutHistoryVM", "Error saving changes", e)
            }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return "Invalid Date"
        val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatDuration(totalSeconds: Long): String {
        if (totalSeconds <= 0) return "00:00:00"
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}