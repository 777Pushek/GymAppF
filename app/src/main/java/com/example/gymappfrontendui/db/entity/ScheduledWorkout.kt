package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gymappfrontendui.models.DayOfWeek

@Entity(
    tableName = "scheduled_workouts",
    foreignKeys = [
        ForeignKey(
            entity = WeekSchedule::class,
            parentColumns = ["week_schedule_id"],
            childColumns = ["week_schedule_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = ["workout_template_id"],
            childColumns = ["workout_template_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("week_schedule_id"), Index("workout_template_id")]
)
data class ScheduledWorkout (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "scheduled_workout_id") val scheduledWorkoutId: Int = 0,
    @ColumnInfo(name = "week_schedule_id") val weekScheduleId: Int,
    @ColumnInfo(name = "workout_template_id") val workoutTemplateId: Int,
    @ColumnInfo(name = "day") val day: DayOfWeek,
    @ColumnInfo(name = "time") val time: String?
)