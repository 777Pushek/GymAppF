package com.example.gymappfrontendui.models

import com.squareup.moshi.Json

enum class NotificationTime(val minutes: Int) {
    @Json(name = "disabled") DISABLED(0),
    @Json(name = "1m") ONE_MINUTE(1),
    @Json(name = "5m") FIVE_MINUTES(5),
    @Json(name = "15m") FIFTEEN_MINUTES(15),
    @Json(name = "30m") THIRTY_MINUTES(30),
    @Json(name = "45m") FORTY_FIVE_MINUTES(45),
    @Json(name = "1h") ONE_HOUR(60),
    @Json(name = "2h") TWO_HOURS(120),
    @Json(name = "3h") THREE_HOURS(180),
    @Json(name = "4h") FOUR_HOURS(240),
    @Json(name = "5h") FIVE_HOURS(300),
    @Json(name = "6h") SIX_HOURS(360)

}