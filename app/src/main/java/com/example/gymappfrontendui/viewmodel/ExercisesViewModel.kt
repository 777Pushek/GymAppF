package com.example.gymappfrontendui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.ExerciseMuscleGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.gymappfrontendui.db.entity.MuscleGroup
import com.example.gymappfrontendui.db.relationships.ExerciseWithMuscleGroups

import com.example.gymappfrontendui.repository.ExerciseRepository
import com.example.gymappfrontendui.repository.MuscleGroupRepository


class ExercisesViewModel(app: Application): AndroidViewModel(app) {

    private val exerciseRepository = ExerciseRepository(app.applicationContext)

    private val muscleGroupRepository = MuscleGroupRepository(app.applicationContext)
    private val sharedPrefs = app.getSharedPreferences("gym_app_prefs", Context.MODE_PRIVATE)
    private val _exercise = MutableStateFlow<Exercise?>(null)

    val exercise = _exercise.asStateFlow()

    fun getAllMuscleGroups(): Flow<List<MuscleGroup>> {
        return muscleGroupRepository.getAllMuscleGroups()
    }
    fun getAvailableExercises(): Flow<List<Exercise>> {
        return exerciseRepository.getAvailableExercises()

    }

    fun getAvailableExercisesWithMuscleGroups(): Flow<List<ExerciseWithMuscleGroups>> {
        return exerciseRepository.getAvailableExercisesWithMuscleGroups()
    }
    suspend fun insertExercise(exercise: Exercise,muscleGroupIds: List<Int>): Long {
        return exerciseRepository.insertExercise(exercise,muscleGroupIds)

    }
    suspend fun insertMuscleGroup(muscleGroup: MuscleGroup): Long {

        return muscleGroupRepository.insertMuscleGroup(muscleGroup)

    }
    suspend fun insertExerciseMuscleGroup(exerciseMuscleGroup: ExerciseMuscleGroup){
        exerciseRepository.insertExerciseMuscleGroup(exerciseMuscleGroup)
    }

    suspend fun deleteMuscleGroup(muscleGroup: MuscleGroup){
        muscleGroupRepository.deleteMuscleGroup(muscleGroup)
    }
    suspend fun deleteExercise(exercise: Exercise){
        exerciseRepository.deleteExercise(exercise)
    }

}