package com.example.gymappfrontendui.network.dto.response

import com.example.gymappfrontendui.models.DayOfWeek
import com.example.gymappfrontendui.models.NotificationTime
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetWeekSchedulesResponse(
    @Json(name = "has_more")
    val hasMore: Boolean,
    @Json(name = "data")
    val data: List<WeekScheduleResponse>
)


@JsonClass(generateAdapter = true)
data class WeekScheduleResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "deleted")
    val deleted: Boolean,
    @Json(name = "name")
    val name: String,
    @Json(name = "selected")
    val selected: Boolean,
    @Json(name = "notification_time")
    val notificationTime: NotificationTime,
    @Json(name = "scheduleWorkouts")
    val scheduleWorkouts: List<ScheduleWorkoutResponse>
)

@JsonClass(generateAdapter = true)
data class ScheduleWorkoutResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "workout_template_id")
    val workoutTemplateId: Int,
    @Json(name = "day")
    val day: DayOfWeek,
    @Json(name = "time")
    val time: String?
)


