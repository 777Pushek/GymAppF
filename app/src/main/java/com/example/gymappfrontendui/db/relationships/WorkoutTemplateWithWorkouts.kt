package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Workout
import com.example.gymappfrontendui.db.entity.WorkoutTemplate

data class WorkoutTemplateWithWorkouts (
    @Embedded val workoutTemplate: WorkoutTemplate,
    @Relation(parentColumn = "workout_template_id", entityColumn = "workout_template_id")
    val workouts: List<Workout>
)