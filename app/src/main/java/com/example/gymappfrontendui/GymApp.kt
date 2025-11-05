package com.example.gymappfrontendui

import android.app.Application
import android.util.Log
import com.example.gymappfrontendui.notifications.WorkoutAlarmScheduler
import com.example.gymappfrontendui.util.ConnectivityListener
import com.example.gymappfrontendui.util.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GymApp : Application() {
    private var connectivityListener: ConnectivityListener? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("GymApp", "App started, initializing background sync...")
        val appCtx = applicationContext

        SyncManager.schedulePeriodicSync(appCtx)
        SyncManager.syncNow(appCtx)

        connectivityListener = ConnectivityListener(appCtx)
        connectivityListener?.start()
        CoroutineScope(Dispatchers.IO).launch {
            WorkoutAlarmScheduler.scheduleNextWorkoutAlarm(appCtx)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        connectivityListener?.stop()
    }
}