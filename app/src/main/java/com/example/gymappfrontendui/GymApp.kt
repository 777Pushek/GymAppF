package com.example.gymappfrontendui

import android.app.Application
import android.util.Log
import com.example.gymappfrontendui.notifications.ReminderSetup
import com.example.gymappfrontendui.util.ConnectivityListener
import com.example.gymappfrontendui.util.SyncManager

class GymApp : Application() {
    private var connectivityListener: ConnectivityListener? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("GymApp", "App started, initializing background sync...")
        val appCtx = applicationContext

        ReminderSetup.scheduleDailyCheck(appCtx)
        SyncManager.schedulePeriodicSync(appCtx)

        connectivityListener = ConnectivityListener(appCtx)
        connectivityListener?.start()

        SyncManager.syncNow(appCtx)
    }

    override fun onTerminate() {
        super.onTerminate()
        connectivityListener?.stop()
    }
}