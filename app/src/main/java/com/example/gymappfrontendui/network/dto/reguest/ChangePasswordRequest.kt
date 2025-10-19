package com.example.gymappfrontendui.network.dto.reguest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "currentPassword")
    val currentPassword: String,
    @Json(name = "newPassword")
    val newPassword: String
)
