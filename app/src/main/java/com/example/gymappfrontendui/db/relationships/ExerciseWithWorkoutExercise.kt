package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.WorkoutExercise

data class ExerciseWithWorkoutExercise (
    @Embedded
    val exercise: Exercise,

    @Relation(parentColumn = "exercise_id", entityColumn = "exercise_id")
    val workoutExercises: List<WorkoutExercise>
)
