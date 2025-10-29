package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Workout
import com.example.gymappfrontendui.db.entity.WorkoutExercise

data class WorkoutWithWorkoutExerciseAndSets (
    @Embedded
    val workout: Workout,
    @Relation(
        entity = WorkoutExercise::class,
        parentColumn = "workout_id",
        entityColumn = "workout_id")
    val exercisesWithSets: List<WorkoutExerciseWithSets>
)