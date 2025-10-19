package com.example.gymappfrontendui.models

import com.example.gymappfrontendui.db.entity.Exercise
data class TemplateExercise(
    val exercise: Exercise,
    val setCount: Int
)

data class WorkoutTemplate(
    val id: Int,
    val name: String,
    val exercises: List<TemplateExercise>
)