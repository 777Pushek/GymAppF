package com.example.gymappfrontendui.network.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyResetCodeResponse(
    @Json(name = "token")
    val token: String,
    @Json(name="message")
    val message: String
)

