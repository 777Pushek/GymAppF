package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["user_id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = WorkoutTemplate::class,
        parentColumns = ["workout_template_id"],
        childColumns = ["workout_template_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("user_id"), Index("workout_template_id")]
)
data class Workout (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "workout_id") val workoutId: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int? = null,
    @ColumnInfo(name = "workout_template_id") val workoutTemplateId: Int?,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "duration") val duration: Int?,
    @ColumnInfo(name = "global_id") var globalId: Int? =null
)
