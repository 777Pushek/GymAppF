package com.example.gymappfrontendui.models

import com.squareup.moshi.Json

enum class NotificationTime(val value: String) {
    @Json(name = "disabled") DISABLED("disabled"),
    @Json(name = "1m") ONE_MINUTE("1m"),
    @Json(name = "5m") FIVE_MINUTES("5m"),
    @Json(name = "15m") FIFTEEN_MINUTES("15m"),
    @Json(name = "30m") THIRTY_MINUTES("30m"),
    @Json(name = "45m") FORTY_FIVE_MINUTES("45m"),
    @Json(name = "1h") ONE_HOUR("1h"),
    @Json(name = "2h") TWO_HOURS("2h"),
    @Json(name = "3h") THREE_HOURS("3h"),
    @Json(name = "4h") FOUR_HOURS("4h"),
    @Json(name = "5h") FIVE_HOURS("5h"),
    @Json(name = "6h") SIX_HOURS("6h")
}