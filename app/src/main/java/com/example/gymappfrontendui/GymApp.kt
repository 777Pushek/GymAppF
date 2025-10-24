package com.example.gymappfrontendui

import android.app.Application
import android.util.Log
import com.example.gymappfrontendui.util.ConnectivityListener
import com.example.gymappfrontendui.util.SyncManager

class GymApp : Application() {
    private var connectivityListener: ConnectivityListener? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("GymApp", "App started, initializing background sync...")

        SyncManager.schedulePeriodicSync(this)

        connectivityListener = ConnectivityListener(this)
        connectivityListener?.start()

        SyncManager.syncNow(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        connectivityListener?.stop()
    }
}