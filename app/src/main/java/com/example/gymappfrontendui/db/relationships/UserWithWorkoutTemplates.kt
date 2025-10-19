package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.User
import com.example.gymappfrontendui.db.entity.WorkoutTemplate

data class UserWithWorkoutTemplates(
    @Embedded val user: User,
    @Relation(parentColumn = "user_id", entityColumn = "user_id")
    val workoutTemplates: List<WorkoutTemplate>
)
