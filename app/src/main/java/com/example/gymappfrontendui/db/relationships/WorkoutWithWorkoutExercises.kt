package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Workout
import com.example.gymappfrontendui.db.entity.WorkoutExercise

data class WorkoutWithWorkoutExercises (
    @Embedded val workout: Workout,
    @Relation(parentColumn = "workout_id", entityColumn = "workout_id")
    var workoutExercises: List<WorkoutExercise>
)
