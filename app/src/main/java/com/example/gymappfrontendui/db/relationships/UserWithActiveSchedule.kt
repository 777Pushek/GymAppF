package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.User
import com.example.gymappfrontendui.db.entity.WeekSchedule

data class UserWithActiveSchedule(
    @Embedded val user: User,
    @Relation(
        parentColumn = "selected_week_schedule_id",
        entityColumn = "week_schedule_id"
    )
    val activeWeekSchedule: WeekSchedule?
)
