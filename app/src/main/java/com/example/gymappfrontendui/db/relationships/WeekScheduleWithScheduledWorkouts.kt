package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.ScheduledWorkout
import com.example.gymappfrontendui.db.entity.WeekSchedule

data class WeekScheduleWithScheduledWorkouts(
    @Embedded
    val weekSchedule: WeekSchedule,

    @Relation(
        parentColumn = "week_schedule_id",
        entityColumn = "week_schedule_id"
    )
    val scheduledWorkouts: List<ScheduledWorkout>
)
