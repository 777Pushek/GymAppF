package com.example.gymappfrontendui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.Set
import com.example.gymappfrontendui.db.entity.Workout
import com.example.gymappfrontendui.db.entity.WorkoutExercise
import com.example.gymappfrontendui.db.entity.WorkoutTemplate
import com.example.gymappfrontendui.db.entity.WorkoutTemplateExercise
import com.example.gymappfrontendui.models.ActiveWorkoutExercise
import com.example.gymappfrontendui.models.ActiveWorkoutSet
import com.example.gymappfrontendui.db.relationships.WorkoutTemplateWithExercises
import com.example.gymappfrontendui.repository.SetRepository
import com.example.gymappfrontendui.repository.UserRepository
import com.example.gymappfrontendui.repository.WorkoutRepository
import com.example.gymappfrontendui.repository.WorkoutTemplateRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class WorkoutUiState {
    object NotStarted : WorkoutUiState()
    data class InProgress(
        val exercises: List<ActiveWorkoutExercise>,
        val elapsedTime: Long
    ) : WorkoutUiState()
}

class WorkoutViewModel(app: Application) : AndroidViewModel(app) {

    private val workoutRepository = WorkoutRepository(app.applicationContext)
    private val userRepository = UserRepository(app.applicationContext)
    private val setRepository  = SetRepository(app.applicationContext)
    private val workoutTemplateRepository = WorkoutTemplateRepository(app.applicationContext)


    private val allTemplatesFlow: StateFlow<List<WorkoutTemplateWithExercises>> =
        workoutTemplateRepository.getAvailableTemplatesWithExercises()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val premadeTemplates: StateFlow<List<WorkoutTemplateWithExercises>> = allTemplatesFlow
        .map { list -> list.filter { it.workoutTemplate.userId == null } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTemplates: StateFlow<List<WorkoutTemplateWithExercises>> = allTemplatesFlow
        .map { list -> list.filter { it.workoutTemplate.userId != null } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.NotStarted)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()
    private val _showConfirmationDialog = MutableStateFlow(false)
    val showConfirmationDialog: StateFlow<Boolean> = _showConfirmationDialog.asStateFlow()
    private var timerJob: Job? = null
    private val activeExercises = mutableListOf<ActiveWorkoutExercise>()

    fun startWorkout() {
        _uiState.value = WorkoutUiState.InProgress(
            exercises = activeExercises,
            elapsedTime = 0L
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value is WorkoutUiState.InProgress) {
                    val currentState = _uiState.value as WorkoutUiState.InProgress
                    _uiState.value = currentState.copy(
                        elapsedTime = currentState.elapsedTime + 1
                    )
                }
            }
        }
    }

    fun removeExerciseFromWorkout(exerciseId: Int) {
        activeExercises.removeAll { it.exercise.exerciseId == exerciseId }
        updateUiInProgressState()
    }

    fun removeSetFromExercise(exerciseId: Int, set: ActiveWorkoutSet) {
        activeExercises.find { it.exercise.exerciseId == exerciseId }?.let { workoutExercise ->
            workoutExercise.sets.remove(set)
            workoutExercise.sets.forEachIndexed { index, currentSet ->
                currentSet.setNumber = index + 1
            }
        }
        updateUiInProgressState()
    }

    fun addExercisesToWorkout(selectedExercises: List<Exercise>) {
        selectedExercises.forEach { exercise ->
            if (activeExercises.none { it.exercise.exerciseId == exercise.exerciseId }) {
                val newWorkoutExercise = ActiveWorkoutExercise(exercise).apply {
                    sets.add(ActiveWorkoutSet(setNumber = 1))
                }
                activeExercises.add(newWorkoutExercise)
            }
        }
        updateUiInProgressState()
    }

    fun addSetToExercise(exerciseId: Int) {
        val exerciseIndex = activeExercises.indexOfFirst { it.exercise.exerciseId == exerciseId }
        if (exerciseIndex != -1) {
            val currentExercise = activeExercises[exerciseIndex]
            val nextSetNumber = currentExercise.sets.size + 1
            val newSet = ActiveWorkoutSet(
                setId = 0,
                workoutExerciseId = currentExercise.workoutExerciseId,
                setNumber = nextSetNumber
            )
            val updatedSets = currentExercise.sets.toMutableList().apply { add(newSet) }
            val updatedExercise = currentExercise.copy(sets = updatedSets)
            activeExercises[exerciseIndex] = updatedExercise
            updateUiInProgressState()
        } else {
            Log.e("WorkoutViewModel", "Exercise with ID $exerciseId not found.")
        }
    }

    private fun updateUiInProgressState() {
        if (_uiState.value is WorkoutUiState.InProgress) {
            val currentState = _uiState.value as WorkoutUiState.InProgress
            _uiState.value = currentState.copy(exercises = activeExercises.toList())
        }
    }

    fun cancelWorkout() {
        timerJob?.cancel()
        activeExercises.clear()
        _uiState.value = WorkoutUiState.NotStarted
    }

    fun saveWorkout(workoutTemplateId: Int? = null) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorkoutUiState.InProgress ?: return@launch
            val hasIncompleteSets = currentState.exercises.any { exercise ->
                exercise.sets.any { !it.isCompleted && (it.weight.isNotEmpty() || it.reps.isNotEmpty()) }
            }
            if (hasIncompleteSets) {
                _showConfirmationDialog.value = true
            } else {
                finalizeWorkout(workoutTemplateId)
            }
        }
    }

    fun confirmSaveChanges(workoutTemplateId: Int? = null) {
        viewModelScope.launch {
            activeExercises.forEach { workoutExercise ->
                workoutExercise.sets.removeAll { !it.isCompleted }
            }
            activeExercises.removeAll { it.sets.isEmpty() }
            finalizeWorkout(workoutTemplateId)
        }
    }

    fun dismissConfirmationDialog() {
        _showConfirmationDialog.value = false
    }

    private suspend fun finalizeWorkout(workoutTemplateId: Int? = null) {
        val currentState = _uiState.value as? WorkoutUiState.InProgress ?: return
        val userId = userRepository.getLoggedInUserId() ?: userRepository.getGuestUserId()

        val workoutId = workoutRepository.insertWorkout(
            Workout(
                userId = userId,
                workoutTemplateId = workoutTemplateId,
                date = System.currentTimeMillis().toString(),
                duration = currentState.elapsedTime.toInt()
            )
        )

        activeExercises.forEachIndexed { exerciseIndex, activeExercise ->
            val workoutExerciseId = workoutRepository.insertWorkoutExercise(
                WorkoutExercise(
                    exerciseId = activeExercise.exercise.exerciseId,
                    workoutId = workoutId.toInt(),
                    position = exerciseIndex
                )
            )

            activeExercise.sets.forEachIndexed { setIndex, activeSet ->
                val weightValue = activeSet.weight.toFloatOrNull() ?: 0f
                val repsValue = activeSet.reps.toIntOrNull() ?: 0

                if (weightValue > 0 || repsValue > 0 || activeSet.isCompleted) {
                    setRepository.insertSet(
                        Set(
                            workoutExerciseId = workoutExerciseId.toInt(),
                            reps = repsValue,
                            weight = weightValue,
                            position = setIndex
                        )
                    )
                }
            }
        }

        _showConfirmationDialog.value = false
        cancelWorkout()
    }

    fun startWorkoutFromTemplate(template: WorkoutTemplateWithExercises) {
        startWorkout()
        activeExercises.clear()

        val sortedExercises = template.exercises
            .sortedBy { it.workoutTemplateExercise.position }
            .map { it.exerciseWithGroups.exercise }

        sortedExercises.forEach { exercise ->
            val newWorkoutExercise = ActiveWorkoutExercise(exercise).apply {
                sets.add(ActiveWorkoutSet(setNumber = 1))
            }
            activeExercises.add(newWorkoutExercise)
        }

        updateUiInProgressState()
    }

    fun createTemplate(name: String, exercises: List<Exercise>, userId: Int?) {
        viewModelScope.launch {

            Log.d("WorkoutViewModel", "Próba stworzenia szablonu. Otrzymano userId: $userId")

            if (userId == null) {
                Log.e("WorkoutViewModel", "BŁĄD: Nie można stworzyć szablonu, ponieważ userId jest null!")
                return@launch
            }

            val newTemplate = WorkoutTemplate(name = name, userId = userId)
            val templateId = workoutTemplateRepository.insertWorkoutTemplate(newTemplate)

            val templateExercises = exercises.mapIndexed { index, exercise ->
                WorkoutTemplateExercise(
                    workoutTemplateId = templateId.toInt(),
                    exerciseId = exercise.exerciseId,
                    position = index
                )
            }

            templateExercises.forEach { exerciseToInsert ->
                workoutTemplateRepository.insertWorkoutTemplateExercise(exerciseToInsert)
            }
        }
    }

    fun formatElapsedTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
    fun deleteTemplate(template: WorkoutTemplateWithExercises) {
        viewModelScope.launch {
            workoutTemplateRepository.deleteWorkoutTemplate(template.workoutTemplate)

        }
    }
}