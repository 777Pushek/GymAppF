package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "muscle_groups")
data class MuscleGroup(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "muscle_group_id") val muscleGroupId: Int = 0,
    @ColumnInfo(name = "name") val name: String,

)
