package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.User
import com.example.gymappfrontendui.db.entity.Workout


data class UserWithExercisesAndSets(
    @Embedded val user: User,
    @Relation(
        entity = Workout::class,
        parentColumn = "user_id",
        entityColumn = "user_id")
    val exercisesWithSets: List<WorkoutWithWorkoutExerciseAndSets>
)
