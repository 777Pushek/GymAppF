package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sets",
    foreignKeys = [ForeignKey(
        entity = WorkoutExercise::class,
        parentColumns = ["workout_exercise_id"],
        childColumns = ["workout_exercise_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("workout_exercise_id")]
)
data class Set (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "set_id") val setId: Int = 0,
    @ColumnInfo(name = "workout_exercise_id") val workoutExerciseId: Int,
    @ColumnInfo(name = "reps") val reps: Int,
    @ColumnInfo(name = "weight") val weight: Float,
    @ColumnInfo(name = "position") val position: Int,
)