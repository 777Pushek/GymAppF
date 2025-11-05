package com.example.gymappfrontendui.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gymappfrontendui.repository.SyncQueueRepository


class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started")
        return try {
            Log.d(TAG, "Background sync started...")
            val repo = SyncQueueRepository(applicationContext)
            repo.sync()
            Log.d(TAG, "Sync finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}")
            Result.retry()
        }
    }
}