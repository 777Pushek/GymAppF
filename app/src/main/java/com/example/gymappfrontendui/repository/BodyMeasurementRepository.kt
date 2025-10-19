package com.example.gymappfrontendui.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.example.gymappfrontendui.db.AppDb

import com.example.gymappfrontendui.db.entity.BodyMeasurement
import com.example.gymappfrontendui.db.entity.SyncQueue
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow


class BodyMeasurementRepository(context: Context) {

    private val db = AppDb.getInstance(context)
    private val bodyMeasurementDao = db.bodyMeasurementDao()
    private val syncQueueDao = db.syncQueueDao()
    private val userDao = db.userDao()

    fun getAvailableBodyMeasurements(): Flow<List<BodyMeasurement>> = flow {
        userDao.getLoggedInUserIdFlow().collect { loggedInUserId ->
            val userId = loggedInUserId ?: userDao.getGuestUserId()
            val measurements = bodyMeasurementDao.getAvailableBodyMeasurements(userId).firstOrNull() ?: emptyList()

            emit(measurements)
        }

    }

    fun getBodyMeasurementById(id: Int): Flow<BodyMeasurement> {
        return bodyMeasurementDao.getBodyMeasurementById(id)
    }

    suspend fun insertBodyMeasurements(bodyMeasurements: List<BodyMeasurement>): List<Long> = withContext(
        Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()

        val newId = bodyMeasurementDao.insertBodyMeasurements(bodyMeasurements.map { it.copy(userId = userId) })

        for(i in newId){
            val q = SyncQueue(
                tableName = "body_measurements",
                localId = i.toInt(),
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }
        newId
    }

    suspend fun insertBodyMeasurement(bodyMeasurements: BodyMeasurement): Long = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        val newId = bodyMeasurementDao.insertBodyMeasurement(bodyMeasurements.copy(userId = userId))

        val q =SyncQueue(
            tableName = "body_measurements",
            localId = newId.toInt(),
            userId = userId
        )
        syncQueueDao.insertSyncQueue(q)
        newId
    }

    suspend fun updateBodyMeasurement(bodyMeasurement: BodyMeasurement) = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        bodyMeasurementDao.updateBodyMeasurement(bodyMeasurement.copy(userId = userId))

        if(syncQueueDao.getSyncQueueByTableName(bodyMeasurement.bodyMeasurementId,"body_measurements") == null){
            val q = SyncQueue(
                tableName = "body_measurements",
                localId = bodyMeasurement.bodyMeasurementId,
                globalId = bodyMeasurement.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }
    }

    suspend fun deleteBodyMeasurement(bodyMeasurement: BodyMeasurement) = withContext(Dispatchers.IO){
        if(bodyMeasurement.globalId != null){
            val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()

            val q = SyncQueue(
                tableName = "body_measurements",
                localId = bodyMeasurement.bodyMeasurementId,
                globalId = bodyMeasurement.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }else{
            val existing = syncQueueDao.getSyncQueueByTableName(bodyMeasurement.bodyMeasurementId,"body_measurements")
            if(existing != null){
                syncQueueDao.deleteSyncQueue(existing)
            }
        }
        bodyMeasurementDao.deleteBodyMeasurement(bodyMeasurement)
    }

    suspend fun deleteBodyMeasurementByGlobalId(id: Int) {
        bodyMeasurementDao.deleteBodyMeasurementByGlobalId(id)
    }
}