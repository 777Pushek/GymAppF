package com.example.gymappfrontendui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.ExerciseMuscleGroup
import com.example.gymappfrontendui.db.entity.MuscleGroup
import com.example.gymappfrontendui.db.entity.User
import com.example.gymappfrontendui.repository.BodyMeasurementRepository
import com.example.gymappfrontendui.repository.ExerciseRepository
import com.example.gymappfrontendui.repository.MuscleGroupRepository
import com.example.gymappfrontendui.repository.SetRepository
import com.example.gymappfrontendui.repository.SyncQueueRepository
import com.example.gymappfrontendui.repository.UserRepository
import com.example.gymappfrontendui.repository.WorkoutRepository
import com.example.gymappfrontendui.repository.WorkoutTemplateRepository

class MainViewModel(app: Application): AndroidViewModel(app) {
    private val bodyMeasurementRepository = BodyMeasurementRepository(app.applicationContext)
    private val exerciseRepository = ExerciseRepository(app.applicationContext)
    private val muscleGroupRepository = MuscleGroupRepository(app.applicationContext)
    private val setRepository = SetRepository(app.applicationContext)
    private val syncQueueRepository = SyncQueueRepository(app.applicationContext)
    private val userRepository = UserRepository(app.applicationContext)
    private val workoutRepository = WorkoutRepository(app.applicationContext)
    private val workoutTemplateRepository = WorkoutTemplateRepository(app.applicationContext)


    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise = _exercise.asStateFlow()


    private val _loginState = MutableStateFlow(false)
    val loginState: StateFlow<Boolean> get() = _loginState
    init {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { users ->
                val loggedIn = users.any { it.isLoggedIn }
                _loginState.value = loggedIn
            }
        }
    }

    fun add(){
        viewModelScope.launch {
            syncQueueRepository.sync()

        }
    }
    fun login(username: String, password: String) {
        viewModelScope.launch {
            userRepository.login(username, password)
        }
    }


    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    fun getAvailableExercises(): Flow<List<Exercise>> {
        return exerciseRepository.getAvailableExercises()

    }
    suspend fun insertExercise(exercise: Exercise,muscleGroupIds: List<Int>): Long {

        return exerciseRepository.insertExercise(
            exercise,
            muscleGroupIds
        )

    }
    suspend fun insertMuscleGroup(muscleGroup: MuscleGroup): Long {

        return muscleGroupRepository.insertMuscleGroup(muscleGroup)

    }

    suspend fun insertExerciseMuscleGroup(exerciseMuscleGroup: ExerciseMuscleGroup){
        exerciseRepository.insertExerciseMuscleGroup(exerciseMuscleGroup)
    }
    fun getUser(): Flow<List<User>> {
        return userRepository.getAllUsers()
    }

    fun getAllMuscleGroups(): Flow<List<MuscleGroup>> {
        return muscleGroupRepository.getAllMuscleGroups()
    }
    suspend fun deleteMuscleGroup(muscleGroup: MuscleGroup){
        muscleGroupRepository.deleteMuscleGroup(muscleGroup)
    }
    suspend fun deleteExercise(exercise: Exercise){
        exerciseRepository.deleteExercise(exercise)
    }


}