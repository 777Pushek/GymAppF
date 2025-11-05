package com.example.gymappfrontendui.network.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserLastSyncResponse(
    @Json(name = "last_sync")
    val lastSync: String
)