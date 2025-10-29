package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymappfrontendui.db.entity.SyncQueue

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncQueue(syncQueue: SyncQueue): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncQueues(syncQueues: List<SyncQueue>): List<Long>

    @Update
    suspend fun updateSyncQueue(syncQueue: SyncQueue)

    @Delete
    suspend fun deleteSyncQueue(syncQueue: SyncQueue)

    @Query("SELECT * FROM sync_queue WHERE user_id = :id")
    suspend fun getSyncQueuesByUserId(id: Int): List<SyncQueue>

    @Query("SELECT * FROM sync_queue WHERE local_id = :id AND table_name = :tn LIMIT 1")
    suspend fun getSyncQueueByTableName(id: Int?, tn: String): SyncQueue?
    @Query("SELECT COUNT(*) > 0 FROM sync_queue WHERE user_id = :userId")
    suspend fun hasSyncQueuesForUser(userId: Int): Boolean


}