package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "body_measurements",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["user_id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("user_id")]
)
data class BodyMeasurement (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "body_measurement_id") val bodyMeasurementId: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int? = null,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "weight") val weight: Float?,
    @ColumnInfo(name = "waist") val waist: Float?,
    @ColumnInfo(name = "forearm") val forearm: Float?,
    @ColumnInfo(name = "chest") val chest: Float?,
    @ColumnInfo(name = "calf") val calf: Float?,
    @ColumnInfo(name = "thigh") val thigh: Float?,
    @ColumnInfo(name = "arm") val arm: Float?,
    @ColumnInfo(name = "hips") val hips: Float?,
    @ColumnInfo(name = "global_id") var globalId: Int? = null

)
