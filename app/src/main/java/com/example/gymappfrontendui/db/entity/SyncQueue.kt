package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "sync_queue",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["user_id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("user_id")]
)
data class SyncQueue (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "sync_queue_id") val syncQueueId: Int = 0,
    @ColumnInfo(name = "table_name") val tableName: String,
    @ColumnInfo(name = "local_id") val localId:Int? =null,
    @ColumnInfo(name = "global_id") var globalId: Int? = null,
    @ColumnInfo(name = "user_id") var userId: Int? = null
)