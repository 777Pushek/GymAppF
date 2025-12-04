package com.example.gymappfrontendui.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.entity.MuscleGroup
import com.example.gymappfrontendui.db.relationships.MuscleGroupWithExercises

class MuscleGroupRepository(context: Context) {
    private val muscleGroupDao = AppDb.getInstance(context).muscleGroupDao()

    suspend fun insertMuscleGroup(muscleGroup: MuscleGroup): Long = withContext(Dispatchers.IO){
        val newId = muscleGroupDao.insertMuscleGroup(muscleGroup)
        newId
    }

    suspend fun updateMuscleGroup(muscleGroup: MuscleGroup) = withContext(Dispatchers.IO){
        muscleGroupDao.updateMuscleGroup(muscleGroup)

    }

    suspend fun deleteMuscleGroup(muscleGroup: MuscleGroup) = withContext(Dispatchers.IO){
        muscleGroupDao.deleteMuscleGroup(muscleGroup)
    }

    fun getAllMuscleGroups(): Flow<List<MuscleGroup>> {
        return muscleGroupDao.getAllMuscleGroups()
    }

    fun getMuscleGroupById(id: Int): Flow<MuscleGroup> {
        return muscleGroupDao.getMuscleGroupById(id)
    }

    fun getMuscleGroupWithExercises(id: Int): Flow<MuscleGroupWithExercises> {
        return muscleGroupDao.getMuscleGroupWithExercises(id)
    }

    fun getAllMuscleGroupsWithExercises(): Flow<List<MuscleGroupWithExercises>> {
        return muscleGroupDao.getAllMuscleGroupsWithExercises()
    }
}