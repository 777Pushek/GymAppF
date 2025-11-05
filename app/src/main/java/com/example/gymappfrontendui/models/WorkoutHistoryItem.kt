package com.example.gymappfrontendui.models

import java.time.LocalDate

data class WorkoutHistoryItem(
    val workoutId: Int,
    val workoutDate: LocalDate,
    val duration: String,
    val totalVolume: Float,
    val exercises: List<ExerciseDetail>
)
data class ExerciseDetail(
    val name: String,
    val setCount: Int,
    val bestSet: String
)