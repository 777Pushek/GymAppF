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
    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Worker started")
        return try {
            Log.d("SyncWorker", "Background sync started...")
            val repo = SyncQueueRepository(applicationContext)
            repo.sync()
            Log.d("SyncWorker", "Sync finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed: ${e.message}")
            Result.retry()
        }
    }
}