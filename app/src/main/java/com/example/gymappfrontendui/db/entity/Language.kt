package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "languages"
)
data class Language (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "language_id") val languageId: Int = 0,
    @ColumnInfo(name = "name") val name: String
)