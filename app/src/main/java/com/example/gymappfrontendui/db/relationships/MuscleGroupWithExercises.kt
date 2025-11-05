package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.ExerciseMuscleGroup
import com.example.gymappfrontendui.db.entity.MuscleGroup

data class MuscleGroupWithExercises (
    @Embedded val muscleGroup: MuscleGroup,
    @Relation(
        parentColumn = "muscle_group_id",
        entityColumn = "exercise_id",
        associateBy = Junction(ExerciseMuscleGroup::class)
    )
    val exercises: List<Exercise>
)

