package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymappfrontendui.db.entity.ScheduledWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledWorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledWorkout(scheduledWorkout: ScheduledWorkout): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledWorkouts(scheduledWorkouts: List<ScheduledWorkout>): List<Long>

    @Update
    suspend fun updateScheduledWorkout(scheduledWorkout: ScheduledWorkout)

    @Delete
    suspend fun deleteScheduledWorkout(scheduledWorkout: ScheduledWorkout)

    @Query("SELECT * FROM scheduled_workouts WHERE scheduled_workout_id = :id")
    fun getScheduledWorkoutById(id: Int): Flow<ScheduledWorkout>

    @Query("SELECT * FROM scheduled_workouts WHERE week_schedule_id = :weekScheduleId ORDER BY day, time")
    fun getScheduledWorkoutsForWeekSchedule(weekScheduleId: Int): List<ScheduledWorkout>

    @Query("SELECT * FROM scheduled_workouts")
    fun getAllScheduledWorkouts(): Flow<List<ScheduledWorkout>>
    @Query("SELECT * FROM scheduled_workouts WHERE week_schedule_id = :weekScheduleId")
    suspend fun getWorkoutsForWeek(weekScheduleId: Int): List<ScheduledWorkout>
}