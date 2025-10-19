package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.Set
import com.example.gymappfrontendui.db.entity.WorkoutExercise

data class WorkoutExerciseWithSets (
    @Embedded val workoutExercise: WorkoutExercise,
    @Relation(parentColumn = "workout_exercise_id", entityColumn = "workout_exercise_id")
    val sets: List<Set>
)
