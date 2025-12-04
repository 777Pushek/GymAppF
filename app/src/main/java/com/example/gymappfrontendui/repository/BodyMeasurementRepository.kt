package com.example.gymappfrontendui.repository

import android.content.Context
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.entity.BodyMeasurement
import com.example.gymappfrontendui.db.entity.SyncQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class BodyMeasurementRepository(context: Context) {

    private val db = AppDb.getInstance(context)
    private val bodyMeasurementDao = db.bodyMeasurementDao()
    private val syncQueueDao = db.syncQueueDao()
    private val userDao = db.userDao()

    fun getAvailableBodyMeasurements(): Flow<List<BodyMeasurement>> {
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId ->
            val userId = loggedInUserId ?: userDao.getGuestUserId()
            bodyMeasurementDao.getAvailableBodyMeasurements(userId)

        }
    }

    fun getBodyMeasurementById(id: Int): Flow<BodyMeasurement> {
        return bodyMeasurementDao.getBodyMeasurementById(id)
    }


    suspend fun insertBodyMeasurement(bodyMeasurement: BodyMeasurement): Long = withContext(Dispatchers.IO) {
        val newId = bodyMeasurementDao.insertBodyMeasurement(bodyMeasurement)
        val q = SyncQueue(
            tableName = "body_measurements",
            localId = newId.toInt(),
            userId = bodyMeasurement.userId
        )
        syncQueueDao.insertSyncQueue(q)
        newId
    }

    suspend fun updateBodyMeasurement(bodyMeasurement: BodyMeasurement) = withContext(Dispatchers.IO) {
        bodyMeasurementDao.updateBodyMeasurement(bodyMeasurement)
        if (bodyMeasurement.globalId != null && syncQueueDao.getSyncQueueByTableName(bodyMeasurement.bodyMeasurementId, "body_measurements") == null) {
            val q = SyncQueue(
                tableName = "body_measurements",
                localId = bodyMeasurement.bodyMeasurementId,
                globalId = bodyMeasurement.globalId,
                userId = bodyMeasurement.userId
            )
            syncQueueDao.insertSyncQueue(q)
        }
    }

    suspend fun deleteBodyMeasurement(bodyMeasurement: BodyMeasurement) = withContext(Dispatchers.IO) {
        if (bodyMeasurement.globalId != null) {
            val userId = bodyMeasurement.userId ?: userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
            val q = SyncQueue(
                tableName = "body_measurements",
                localId = bodyMeasurement.bodyMeasurementId,
                globalId = bodyMeasurement.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        } else {
            val existing = syncQueueDao.getSyncQueueByTableName(bodyMeasurement.bodyMeasurementId, "body_measurements")
            existing?.let { syncQueueDao.deleteSyncQueue(it) }
        }
        bodyMeasurementDao.deleteBodyMeasurement(bodyMeasurement)
    }

    suspend fun deleteBodyMeasurementByGlobalId(id: Int) = withContext(Dispatchers.IO) {
        bodyMeasurementDao.deleteBodyMeasurementByGlobalId(id)
    }
}