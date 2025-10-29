package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.WorkoutTemplate
import com.example.gymappfrontendui.db.entity.WorkoutTemplateExercise

data class ExerciseWithWorkoutTemplates (
    @Embedded
    val exercise: Exercise,

    @Relation(
        parentColumn = "exercise_id", entityColumn = "workout_template_id", associateBy = Junction(
            WorkoutTemplateExercise::class
        )
    )
    val templates: List<WorkoutTemplate>
)

