package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.User


data class UserWithExercises(
    @Embedded val user: User,
    @Relation(parentColumn = "user_id", entityColumn = "user_id")
    val exercises: List<Exercise>
)