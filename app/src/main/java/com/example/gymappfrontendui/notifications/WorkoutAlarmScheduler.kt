package com.example.gymappfrontendui.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.models.NotificationTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat


object WorkoutAlarmScheduler {
    private const val TAG = "WorkoutAlarmScheduler"

    suspend fun scheduleNextWorkoutAlarm(context: Context) = withContext(Dispatchers.IO) {

        val db = AppDb.getInstance(context)
        val weekScheduleDao = db.weekScheduleDao()
        val scheduledWorkoutDao = db.scheduledWorkoutDao()
        val userDao = db.userDao()

        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        val activeSchedule = weekScheduleDao.getSelectedScheduleForUser(userId)
        if (activeSchedule == null) {
            Log.d(TAG, "No active schedule for user $userId")
            return@withContext
        }
        if (activeSchedule.notificationTime == NotificationTime.DISABLED) {
            Log.d(TAG, "Notifications disabled in schedule")
            return@withContext
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled for the app (system setting). Aborting scheduling.")
            return@withContext
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Missing POST_NOTIFICATIONS permission. Aborting scheduling.")
                return@withContext
            }
        }

        val workouts = scheduledWorkoutDao.getWorkoutsForWeek(activeSchedule.weekScheduleId)

        val nowLocalDate = LocalDate.now()
        val nowTime = LocalTime.now()

        val nextWorkout = workouts
            .mapNotNull { w ->
                w.time?.let { timeStr ->
                    val workoutTime = LocalTime.parse(timeStr)
                    val reminderTime = workoutTime.minusMinutes(activeSchedule.notificationTime.minutes.toLong())

                    val workoutDayJava = w.day.toJava()
                    var targetDate = nowLocalDate.with(workoutDayJava)

                    if (targetDate.isBefore(nowLocalDate) ||
                        (targetDate.isEqual(nowLocalDate) && reminderTime.isBefore(nowTime))) {
                        targetDate = targetDate.plusWeeks(1)
                    }


                    LocalDateTime.of(targetDate, reminderTime) to w
                }
            }
            .minByOrNull { it.first } ?: run {
            Log.d(TAG, "No upcoming workouts found")
            return@withContext
            }


        val triggerMillis = nextWorkout.first.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val workout = nextWorkout.second

        val intent = Intent(context, WorkoutAlarmReceiver::class.java).apply {
            putExtra("time", workout.time)
            putExtra("day", workout.day.value)
            putExtra("minutes_before", activeSchedule.notificationTime.minutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled alarm for workout ${workout.scheduledWorkoutId} at $triggerMillis")
            } else {
                Log.w(TAG, "App cannot schedule exact alarms, requesting user to allow")
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(settingsIntent)
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling alarm: ${e.message}", e)
        }
    }
}