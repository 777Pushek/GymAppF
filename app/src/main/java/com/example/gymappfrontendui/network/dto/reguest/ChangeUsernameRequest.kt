package com.example.gymappfrontendui.network.dto.reguest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChangeUsernameRequest(
    @Json(name = "username")
    val username: String,
    @Json(name = "password")
    val password: String
)
