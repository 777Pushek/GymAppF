package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "exercises_muscle_groups",
    primaryKeys = ["muscle_group_id", "exercise_id"],
    foreignKeys = [ForeignKey(
        entity = MuscleGroup::class,
        parentColumns = ["muscle_group_id"],
        childColumns = ["muscle_group_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Exercise::class,
        parentColumns = ["exercise_id"],
        childColumns = ["exercise_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("muscle_group_id"), Index("exercise_id")]
)
data class ExerciseMuscleGroup (
    @ColumnInfo(name = "muscle_group_id") val muscleGroupId: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int
)