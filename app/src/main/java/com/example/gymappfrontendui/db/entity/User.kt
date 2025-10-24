package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "user_id") val userId: Int = 0,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "isLoggedIn") val isLoggedIn: Boolean = false,
    @ColumnInfo(name = "last_sync") val lastSync: String? = null,
    @ColumnInfo(name = "isGuest") val isGuest: Boolean = false,
    @ColumnInfo(name = "selected_week_schedule_id") val selectedWeekScheduleId: Int? = null

)

