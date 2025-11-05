package com.example.gymappfrontendui.db.dto

import androidx.room.Embedded
import com.example.gymappfrontendui.db.entity.Set

data class WorkoutSetWithDate(
    @Embedded val workoutSet: Set,
    val workoutDate: String
)