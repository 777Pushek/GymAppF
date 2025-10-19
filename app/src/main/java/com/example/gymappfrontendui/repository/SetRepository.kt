package com.example.gymappfrontendui.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.dao.SetDao
import com.example.gymappfrontendui.db.entity.Set

class SetRepository(context: Context) {
    private val setDao = AppDb.getInstance(context).setDao()

    suspend fun insertSet(set: Set): Long = withContext(Dispatchers.IO){
        val newId = setDao.insertSet(set)
        newId
    }

    suspend fun insertSets(sets: List<Set>): List<Long> = withContext(Dispatchers.IO){
        val newId = setDao.insertSets(sets)
        newId
    }

    suspend fun updateSet(set: Set) = withContext(Dispatchers.IO){
        setDao.updateSet(set)
    }

    suspend fun deleteSet(set: Set) = withContext(Dispatchers.IO){
        setDao.deleteSet(set)
    }

    fun getSetById(setId: Int): Flow<Set> {
        return setDao.getSetById(setId)
    }

    fun getSetByWorkoutExerciseId(workoutExerciseId: Int): Flow<List<Set>> {
        return setDao.getSetByWorkoutExerciseId(workoutExerciseId)
    }
}