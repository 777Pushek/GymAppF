package com.example.gymappfrontendui.models

import com.squareup.moshi.Json

enum class DayOfWeek(val value: String) {
    @Json(name = "Monday") MONDAY("Monday"),
    @Json(name = "Tuesday") TUESDAY("Tuesday"),
    @Json(name = "Wednesday") WEDNESDAY("Wednesday"),
    @Json(name = "Thursday") THURSDAY("Thursday"),
    @Json(name = "Friday") FRIDAY("Friday"),
    @Json(name = "Saturday") SATURDAY("Saturday"),
    @Json(name = "Sunday") SUNDAY("Sunday")
}