package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "exercises_translations",
    primaryKeys = ["exercise_id", "language_id"],
    foreignKeys = [ForeignKey(
        entity = Exercise::class,
        parentColumns = ["exercise_id"],
        childColumns = ["exercise_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Language::class,
        parentColumns = ["language_id"],
        childColumns = ["language_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("exercise_id"), Index("language_id")]
)
data class ExerciseTranslation (
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    @ColumnInfo(name = "language_id") val languageId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,

)

