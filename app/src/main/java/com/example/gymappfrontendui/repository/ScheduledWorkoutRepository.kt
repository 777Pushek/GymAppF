package com.example.gymappfrontendui.repository

import android.content.Context
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.entity.ScheduledWorkout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScheduledWorkoutRepository(context: Context) {
    private val db = AppDb.getInstance(context)
    private val scheduledWorkoutDao = db.scheduledWorkoutDao()
    suspend fun insertScheduledWorkout(scheduledWorkout: ScheduledWorkout): Long  = withContext(Dispatchers.IO){
        val newId = scheduledWorkoutDao.insertScheduledWorkout(scheduledWorkout)
        newId
    }

    suspend fun insertScheduledWorkouts(scheduledWorkouts: List<ScheduledWorkout>): List<Long>  = withContext(Dispatchers.IO){
        val newId = scheduledWorkoutDao.insertScheduledWorkouts(scheduledWorkouts)
        newId
    }

    suspend fun updateScheduledWorkout(scheduledWorkout: ScheduledWorkout)  = withContext(Dispatchers.IO){
        scheduledWorkoutDao.updateScheduledWorkout(scheduledWorkout)
    }

    suspend fun deleteScheduledWorkout(scheduledWorkout: ScheduledWorkout)  = withContext(Dispatchers.IO){
        scheduledWorkoutDao.deleteScheduledWorkout(scheduledWorkout)
    }

    fun getScheduledWorkoutById(id: Int): Flow<ScheduledWorkout> {
        return scheduledWorkoutDao.getScheduledWorkoutById(id)
    }

    fun getAllScheduledWorkouts(): Flow<List<ScheduledWorkout>> {
        return scheduledWorkoutDao.getAllScheduledWorkouts()
    }
}