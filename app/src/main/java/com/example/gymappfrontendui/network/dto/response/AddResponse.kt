package com.example.gymappfrontendui.network.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddResponse (
    @Json(name = "id")
    val id: Int,
    @Json(name = "message")
    val message: String,
    @Json(name = "last_sync")
    val lastSync: String
)