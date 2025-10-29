package com.example.gymappfrontendui.network.dto.reguest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WorkoutRequest(
    @Json(name = "duration")
    val duration: Int? = null,
    @Json(name = "template")
    val template: Int? = null,
    @Json(name = "date")
    val date: String,
    @Json(name = "exercises")
    val exercises: List<WorkoutExerciseRequest>
)

@JsonClass(generateAdapter = true)
data class WorkoutExerciseRequest(
    @Json(name = "id")
    val id: Int,
    @Json(name = "sets")
    val sets: List<SetRequest>
)
@JsonClass(generateAdapter = true)
data class SetRequest(
    @Json(name = "reps")
    val reps: Int,
    @Json(name = "weight")
    val weight: Float
)

