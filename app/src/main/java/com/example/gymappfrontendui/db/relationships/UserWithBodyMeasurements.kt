package com.example.gymappfrontendui.db.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymappfrontendui.db.entity.BodyMeasurement
import com.example.gymappfrontendui.db.entity.User


data class UserWithBodyMeasurements(
    @Embedded val user: User,
    @Relation(parentColumn = "user_id", entityColumn = "user_id")
    val bodyMeasurements: List<BodyMeasurement>
)
