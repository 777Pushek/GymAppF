package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "muscle_groups_translations",
    primaryKeys = ["muscle_group_id", "language_id"],
    foreignKeys = [ForeignKey(
        entity = MuscleGroup::class,
        parentColumns = ["muscle_group_id"],
        childColumns = ["muscle_group_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Language::class,
        parentColumns = ["language_id"],
        childColumns = ["language_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("muscle_group_id"), Index("language_id")]
)
data class MuscleGroupTranslation (
    @ColumnInfo(name = "muscle_group_id") val muscleGroupId: Int,
    @ColumnInfo(name = "language_id") val languageId: Int,
    @ColumnInfo(name = "name") val name: String
)