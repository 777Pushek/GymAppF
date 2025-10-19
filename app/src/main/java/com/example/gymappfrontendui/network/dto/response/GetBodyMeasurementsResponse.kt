package com.example.gymappfrontendui.network.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BodyMeasurementResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "deleted")
    val deleted: Boolean,
    @Json(name = "weight")
    val weight: Float?,
    @Json(name = "arm")
    val arm: Float?,
    @Json(name = "forearm")
    val forearm: Float?,
    @Json(name = "chest")
    val chest: Float?,
    @Json(name = "waist")
    val waist: Float?,
    @Json(name = "hips")
    val hips: Float?,
    @Json(name = "thigh")
    val thigh: Float?,
    @Json(name = "calf")
    val calf: Float?,
    @Json(name = "date")
    val date: String
)

@JsonClass(generateAdapter = true)
data class GetBodyMeasurementsResponse(
    @Json(name = "has_more")
    val hasMore: Boolean,
    @Json(name = "data")
    val data: List<BodyMeasurementResponse>

)
