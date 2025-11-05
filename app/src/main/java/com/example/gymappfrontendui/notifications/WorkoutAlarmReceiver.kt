package com.example.gymappfrontendui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gymappfrontendui.R


class WorkoutAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val workoutTime = intent.getStringExtra("time") ?: ""
        val workoutDay = intent.getStringExtra("day") ?: ""
        val minutesBefore = intent.getIntExtra("minutes_before", 0)

        val channelId = "workout_reminders"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Workout Reminders", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }
        val title = "Time to workout!"
        val message = "Today at $workoutTime — $workoutDay 💪 (reminder $minutesBefore minutes before)"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.biceps)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(System.currentTimeMillis().hashCode(), notification)
    }
}
