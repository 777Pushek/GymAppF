
package com.example.gymappfrontendui.models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.gymappfrontendui.db.entity.Exercise

data class ActiveWorkoutSet(
    val setId: Int = 0,
    val workoutExerciseId: Int = 0,
    var setNumber: Int,
    var weight: String = "",
    var reps: String = "",
    var isCompleted: Boolean = false,
    val previousPerformance: String = "-"
)

    data class ActiveWorkoutExercise(
        val exercise: Exercise,
        val workoutExerciseId: Int = 0,
        val sets: MutableList<ActiveWorkoutSet> = mutableListOf()
    )
