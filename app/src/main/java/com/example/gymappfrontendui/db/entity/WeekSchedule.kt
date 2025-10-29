package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gymappfrontendui.models.NotificationTime


@Entity(
    tableName = "week_schedules",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id")]
)
data class WeekSchedule(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "week_schedule_id") val weekScheduleId: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "notification_time") val notificationTime: NotificationTime,
    @ColumnInfo(name ="selected") val selected: Boolean = false,
    @ColumnInfo(name = "global_id") var globalId: Int? = null

)
