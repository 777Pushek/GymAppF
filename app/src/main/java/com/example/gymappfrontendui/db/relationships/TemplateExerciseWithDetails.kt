package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.WorkoutTemplateExercise

data class TemplateExerciseWithDetails(
    @Embedded
    val workoutTemplateExercise: WorkoutTemplateExercise,

    @Relation(
        entity = Exercise::class,
        parentColumn = "exercise_id",
        entityColumn = "exercise_id"
    )
    val exerciseWithGroups: ExerciseWithMuscleGroups

)