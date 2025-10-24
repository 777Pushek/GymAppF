package com.example.gymappfrontendui.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.entity.Set
import com.example.gymappfrontendui.db.pojo.WorkoutSetWithDate
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class SetRepository(context: Context) {
    private val db = AppDb.getInstance(context)
    private val setDao = db.setDao()
    private val userDao = db.userDao()

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
    fun getWorkoutSetsWithDateForExercise(exerciseId: Int): Flow<List<WorkoutSetWithDate>> {
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId: Int? ->
            val userId: Int? = loggedInUserId ?: userDao.getGuestUserId()

            if (userId == null) {
                flowOf(emptyList())
            } else {
                setDao.getWorkoutSetsWithDateForExerciseAndUser(exerciseId, userId)
            }
        }
    }
}