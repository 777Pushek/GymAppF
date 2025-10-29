package com.example.gymappfrontendui.worker

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gymappfrontendui.R
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.models.NotificationTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class WorkoutReminderWorker(
    private val ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        Log.e("WORKER-NOTIFI","AAAAAAAAAAAAAAAA");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("WORKER-NOTIFI","BBBBBBBBBBBBBBBBBBBBBBBBb");
            return Result.success()
        }
        Log.e("WORKER-NOTIFI","CCCCCCCCCCCCCCCCCCCCCCCCCCCcc");
        val db = AppDb.getInstance(ctx)
        val weekScheduleDao = db.weekScheduleDao()
        val scheduledWorkoutDao = db.scheduledWorkoutDao()

        val activeSchedule = weekScheduleDao.getSelectedSchedule() ?: return Result.success()

        if (activeSchedule.notificationTime == NotificationTime.DISABLED) return Result.success()

        val workouts = scheduledWorkoutDao.getWorkoutsForWeek(activeSchedule.weekScheduleId)
        val today = LocalDate.now().dayOfWeek.name
        val todayWorkout = workouts.find { it.day.name == today } ?: return Result.success()

        todayWorkout.time?.let { timeStr ->
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val workoutTime = LocalTime.parse(timeStr, formatter)
            val reminderTime = workoutTime.minusMinutes(activeSchedule.notificationTime.minutes.toLong())
            val now = LocalTime.now()


            val windowMinutes = 5L
            if (now.isAfter(reminderTime.minusMinutes(windowMinutes)) &&
                now.isBefore(reminderTime.plusMinutes(windowMinutes))
            ) {
                showNotification(todayWorkout, activeSchedule.notificationTime.minutes)
            }


        }

        return Result.success()
    }

    private fun showNotification(workout: com.example.gymappfrontendui.db.entity.ScheduledWorkout, minutesBefore: Int) {
        val channelId = "workout_reminders"
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = "Time to workout!"
        val message = "Today at ${workout.time} — ${workout.day.value} 💪 (reminder $minutesBefore minutes before)"

        val notification = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.biceps)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()


        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
