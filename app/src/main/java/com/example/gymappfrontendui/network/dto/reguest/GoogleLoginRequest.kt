package com.example.gymappfrontendui.network.dto.reguest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleLoginRequest(
    @Json(name = "idToken")
    val idToken: String
)