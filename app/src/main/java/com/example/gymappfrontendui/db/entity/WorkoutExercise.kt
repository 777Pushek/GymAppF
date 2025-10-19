package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [ForeignKey(
        entity = Workout::class,
        parentColumns = ["workout_id"],
        childColumns = ["workout_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Exercise::class,
        parentColumns = ["exercise_id"],
        childColumns = ["exercise_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("workout_id"), Index("exercise_id")]
)
data class WorkoutExercise (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "workout_exercise_id") val workoutExerciseId: Int = 0,
    @ColumnInfo(name = "exercise_id")val exerciseId: Int,
    @ColumnInfo(name = "workout_id")val workoutId: Int,
    @ColumnInfo(name = "position")val position: Int = 1,

)
