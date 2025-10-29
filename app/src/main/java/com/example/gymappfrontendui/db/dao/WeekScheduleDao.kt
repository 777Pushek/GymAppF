package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gymappfrontendui.db.entity.WeekSchedule
import com.example.gymappfrontendui.db.relationships.WeekScheduleWithScheduledWorkouts
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeekSchedule(weekSchedule: WeekSchedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeekSchedules(weekSchedules: List<WeekSchedule>): List<Long>

    @Update
    suspend fun updateWeekSchedule(weekSchedule: WeekSchedule)

    @Delete
    suspend fun deleteWeekSchedule(weekSchedule: WeekSchedule)

    @Query("DELETE FROM week_schedules WHERE global_id = :globalId")
    suspend fun deleteWeekScheduleByGlobalId(globalId: Int)

    @Query("SELECT * FROM week_schedules WHERE week_schedule_id = :id")
    fun getWeekScheduleById(id: Int): Flow<WeekSchedule>

    @Query("SELECT * FROM week_schedules WHERE user_id = :userId")
    fun getWeekSchedulesByUserId(userId: Int): Flow<List<WeekSchedule>>

    @Query("SELECT * FROM week_schedules")
    fun getAllWeekSchedules(): Flow<List<WeekSchedule>>

    @Transaction
    @Query("SELECT * FROM week_schedules WHERE user_id = :userId")
    fun getWeekSchedulesWithScheduleWorkouts(userId: Int): Flow<List<WeekScheduleWithScheduledWorkouts>>
    @Query("SELECT COUNT(*) > 0 FROM week_schedules WHERE user_id = :userId")
    suspend fun hasWeekSchedulesForUser(userId: Int): Boolean

    @Query("UPDATE week_schedules SET selected = 0 WHERE user_id = :userId")
    suspend fun clearSelectedForUser(userId: Int)
    @Query("SELECT * FROM week_schedules WHERE selected = 1 LIMIT 1")
    suspend fun getSelectedSchedule(): WeekSchedule?

}