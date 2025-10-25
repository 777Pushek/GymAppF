package com.example.gymappfrontendui.db

import androidx.room.TypeConverter
import com.example.gymappfrontendui.models.AccountType
import com.example.gymappfrontendui.models.DayOfWeek
import com.example.gymappfrontendui.models.NotificationTime

class Converters {

    // NotificationTime
    @TypeConverter
    fun fromNotificationTime(value: NotificationTime?): String? = value?.value

    @TypeConverter
    fun toNotificationTime(value: String?): NotificationTime? =
        value?.let { v -> NotificationTime.entries.firstOrNull { it.value == v } }

    // DayOfWeek
    @TypeConverter
    fun fromDayOfWeek(day: DayOfWeek?): String? = day?.value

    @TypeConverter
    fun toDayOfWeek(value: String?): DayOfWeek? =
        value?.let { v -> DayOfWeek.entries.firstOrNull { it.value == v } }

    @TypeConverter
    fun fromAccountType(value: AccountType?): String? = value?.name

    @TypeConverter
    fun toAccountType(value: String?): AccountType? =
        value?.let { AccountType.valueOf(it) }
}