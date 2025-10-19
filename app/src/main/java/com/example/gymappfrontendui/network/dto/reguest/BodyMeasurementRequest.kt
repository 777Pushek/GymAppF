package com.example.gymappfrontendui.network.dto.reguest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BodyMeasurementRequest(
    @Json(name = "weight")
    val weight: Float? = null,
    @Json(name = "arm")
    val arm: Float? = null,
    @Json(name = "forearm")
    val forearm: Float? = null,
    @Json(name = "chest")
    val chest: Float? = null,
    @Json(name = "waist")
    val waist: Float? = null,
    @Json(name = "hips")
    val hips: Float? = null,
    @Json(name = "thigh")
    val thigh: Float? = null,
    @Json(name = "calf")
    val calf: Float? = null,
    @Json(name = "date")
    val date: String
)