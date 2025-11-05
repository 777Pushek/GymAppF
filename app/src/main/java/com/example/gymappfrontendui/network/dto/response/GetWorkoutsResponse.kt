package com.example.gymappfrontendui.network.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetWorkoutsResponse(
    @Json(name = "has_more")
    val hasMore: Boolean,
    @Json(name = "data")
    val data: List<WorkoutResponse>
)
@JsonClass(generateAdapter = true)
data class WorkoutResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "deleted")
    val deleted: Boolean,
    @Json(name = "duration")
    val duration: Int?,
    @Json(name = "date")
    val date: String,
    @Json(name = "template")
    val template: Int?,
    @Json(name = "exercises")
    val exercises: List<WorkoutExerciseResponse>
)

@JsonClass(generateAdapter = true)
data class WorkoutExerciseResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "position")
    val position: Int,
    @Json(name = "sets")
    val sets: List<WorkoutSetResponse>
)

@JsonClass(generateAdapter = true)
data class WorkoutSetResponse(
    @Json(name = "reps")
    val reps: Int,
    @Json(name = "weight")
    val weight: Float
)
