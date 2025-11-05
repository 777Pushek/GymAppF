package com.example.gymappfrontendui.network.dto.reguest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExerciseRequest(
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "muscleGroups")
    val muscleGroups: List<Int>? = null
)
