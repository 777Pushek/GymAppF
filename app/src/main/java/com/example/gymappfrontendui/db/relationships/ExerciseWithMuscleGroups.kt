package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.ExerciseMuscleGroup
import com.example.gymappfrontendui.db.entity.MuscleGroup

data class ExerciseWithMuscleGroups (
    @Embedded
    val exercise: Exercise,

    @Relation(
        parentColumn = "exercise_id",
        entityColumn = "muscle_group_id",
        associateBy = Junction(ExerciseMuscleGroup::class)
    )
    val muscleGroups: List<MuscleGroup>
)

