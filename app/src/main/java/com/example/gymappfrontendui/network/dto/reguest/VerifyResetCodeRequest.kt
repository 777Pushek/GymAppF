package com.example.gymappfrontendui.network.dto.reguest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyResetCodeRequest(
    @Json(name = "username")
    val username: String,
    @Json(name = "code")
    val code: String
)
