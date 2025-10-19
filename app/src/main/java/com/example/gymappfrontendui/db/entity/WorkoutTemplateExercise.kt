package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "workout_template_exercise",
    primaryKeys = ["workout_template_id", "exercise_id"],
    foreignKeys = [ForeignKey(
        entity = WorkoutTemplate::class,
        parentColumns = ["workout_template_id"],
        childColumns = ["workout_template_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Exercise::class,
        parentColumns = ["exercise_id"],
        childColumns = ["exercise_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("workout_template_id"), Index("exercise_id")]
)
data class WorkoutTemplateExercise (
    @ColumnInfo(name = "workout_template_id") val workoutTemplateId: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId:Int,
    @ColumnInfo(name = "position") val position:Int = 1


)