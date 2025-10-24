package com.example.gymappfrontendui.network.dto.reguest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)

data class WeekScheduleRequest(
    @Json(name = "name")
    val name: String,
    @Json(name = "selected")
    val selected: Boolean? = null,
    @Json(name = "scheduleWorkouts")
    val scheduledWorkouts: List<ScheduledWorkoutsRequest>
)

@JsonClass(generateAdapter = true)
data class ScheduledWorkoutsRequest(
    @Json(name = "templateId")
    val templateId: Int,
    @Json(name = "day")
    val day: String,
    @Json(name = "time")
    val time: String?
)

