package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.WorkoutTemplate
import com.example.gymappfrontendui.db.entity.WorkoutTemplateExercise

data class WorkoutTemplateWithExercises (
    @Embedded val workoutTemplate: WorkoutTemplate,
    @Relation(
        entity = WorkoutTemplateExercise::class,

        parentColumn = "workout_template_id",

        entityColumn = "workout_template_id"
    )
    val exercises: List<TemplateExerciseWithDetails>
)