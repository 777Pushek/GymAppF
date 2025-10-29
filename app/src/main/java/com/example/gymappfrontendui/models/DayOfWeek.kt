package com.example.gymappfrontendui.models

import com.squareup.moshi.Json
import java.time.DayOfWeek as JavaDayOfWeek

enum class DayOfWeek(val value: String) {
    @Json(name = "Monday") MONDAY("Monday"),
    @Json(name = "Tuesday") TUESDAY("Tuesday"),
    @Json(name = "Wednesday") WEDNESDAY("Wednesday"),
    @Json(name = "Thursday") THURSDAY("Thursday"),
    @Json(name = "Friday") FRIDAY("Friday"),
    @Json(name = "Saturday") SATURDAY("Saturday"),
    @Json(name = "Sunday") SUNDAY("Sunday");
    fun toJava(): JavaDayOfWeek = JavaDayOfWeek.valueOf(this.name)

    companion object {
        fun fromJava(day: JavaDayOfWeek): DayOfWeek = valueOf(day.name)
    }
}