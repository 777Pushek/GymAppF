package com.example.gymappfrontendui.repository

import android.content.Context
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.entity.SyncQueue
import com.example.gymappfrontendui.db.entity.WeekSchedule
import com.example.gymappfrontendui.db.relationships.WeekScheduleWithScheduledWorkouts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WeekScheduleRepository(context: Context) {
    private val db = AppDb.getInstance(context)
    private val weekScheduleDao = db.weekScheduleDao()
    private val syncQueueDao = db.syncQueueDao()
    private val userDao = db.userDao()
    suspend fun insertWeekSchedule(weekSchedule: WeekSchedule): Long = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        val newId = weekScheduleDao.insertWeekSchedule(weekSchedule.copy(userId = userId))

        val q = SyncQueue(
            tableName = "week_schedules",
            localId = newId.toInt(),
            userId = userId
        )
        syncQueueDao.insertSyncQueue(q)
        newId
    }

    suspend fun updateWeekSchedule(weekSchedule: WeekSchedule) = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        weekScheduleDao.updateWeekSchedule(weekSchedule.copy(userId = userId))
        if(syncQueueDao.getSyncQueueByTableName(weekSchedule.weekScheduleId,"week_schedules") == null){
            val q = SyncQueue(
                tableName = "week_schedules",
                localId = weekSchedule.weekScheduleId,
                globalId = weekSchedule.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }
    }

    suspend fun deleteWeekSchedule(weekSchedule: WeekSchedule) = withContext(Dispatchers.IO){
        if(weekSchedule.globalId != null){
            val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
            val q = SyncQueue(
                tableName = "week_schedules",
                localId = weekSchedule.weekScheduleId,
                globalId = weekSchedule.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }else{
            val existing = syncQueueDao.getSyncQueueByTableName(weekSchedule.weekScheduleId,"week_schedules")
            if(existing != null){
                syncQueueDao.deleteSyncQueue(existing)
            }
        }
        weekScheduleDao.deleteWeekSchedule(weekSchedule)
    }
    fun getWeekScheduleById(id: Int): Flow<WeekSchedule> {
        return weekScheduleDao.getWeekScheduleById(id)
    }

    fun getWeekSchedulesByUserId(userId: Int): Flow<List<WeekSchedule>> {
        return weekScheduleDao.getWeekSchedulesByUserId(userId)
    }

    fun getAllWeekSchedules(): Flow<List<WeekSchedule>> {
        return weekScheduleDao.getAllWeekSchedules()
    }

    fun getWeekSchedulesWithScheduleWorkouts(userId: Int): Flow<List<WeekScheduleWithScheduledWorkouts>> {
        return weekScheduleDao.getWeekSchedulesWithScheduleWorkouts(userId)
    }
}