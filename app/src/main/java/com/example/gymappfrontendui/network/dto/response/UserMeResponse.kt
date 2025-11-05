package com.example.gymappfrontendui.network.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserMeResponse(
    @Json(name = "googleAccount")
    val googleAccount: Boolean,
    @Json(name = "email")
    val email: String?,
    @Json(name = "username")
    val userName: String,
    @Json(name = "emailVerified")
    val emailVerified: Boolean
)
