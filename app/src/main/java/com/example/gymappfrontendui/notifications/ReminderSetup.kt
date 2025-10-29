package com.example.gymappfrontendui.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gymappfrontendui.worker.WorkoutReminderWorker
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object ReminderSetup {
    fun scheduleDailyCheck(context: Context) {
        val request = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(
            1, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "hourly_workout_check",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }


}
