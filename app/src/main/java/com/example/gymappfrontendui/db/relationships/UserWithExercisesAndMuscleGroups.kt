package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.User


data class UserWithExercisesAndMuscleGroups(
    @Embedded val user: User,
    @Relation(
        entity = Exercise::class,
        parentColumn = "user_id",
        entityColumn = "user_id")
    val exercisesWithMuscleGroups: List<ExerciseWithMuscleGroups>

)
