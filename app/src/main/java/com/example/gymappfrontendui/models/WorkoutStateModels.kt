
package com.example.gymappfrontendui.models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.gymappfrontendui.db.entity.Exercise

data class ActiveWorkoutSet(
    var setNumber: Int,
    var weight: String = "",
    var reps: String = "",
    var isCompleted: Boolean = false,
    val previousPerformance: String = "–"
)

data class ActiveWorkoutExercise(
    val exercise: Exercise,
    var sets: SnapshotStateList<ActiveWorkoutSet> = mutableStateListOf()
)