package com.example.gymappfrontendui.network.dto.reguest

import com.example.gymappfrontendui.models.DayOfWeek
import com.example.gymappfrontendui.models.NotificationTime
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)

data class WeekScheduleRequest(
    @Json(name = "name")
    val name: String,
    @Json(name = "selected")
    val selected: Boolean? = null,
    @Json (name = "notification_time")
    val notificationTime: NotificationTime,
    @Json(name = "scheduleWorkouts")
    val scheduledWorkouts: List<ScheduledWorkoutsRequest>
)

@JsonClass(generateAdapter = true)
data class ScheduledWorkoutsRequest(
    @Json(name = "templateId")
    val templateId: Int,
    @Json(name = "day")
    val day: DayOfWeek,
    @Json(name = "time")
    val time: String?
)

