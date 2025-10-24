package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.ScheduledWorkout
import com.example.gymappfrontendui.db.entity.WorkoutTemplate

data class WorkoutTemplateWithScheduledWorkouts(
    @Embedded
    val workoutTemplate: WorkoutTemplate,

    @Relation(
        parentColumn = "workout_template_id",
        entityColumn = "workout_template_id"
    )
    val scheduledWorkouts: List<ScheduledWorkout>
)