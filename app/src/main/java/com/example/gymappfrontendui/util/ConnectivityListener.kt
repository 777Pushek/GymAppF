package com.example.gymappfrontendui.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log

class ConnectivityListener(
    private val context: Context
) {
    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    companion object {
        private const val TAG = "ConnectivityListener"
    }

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(TAG, "Network available. Triggering sync...")
            try {
                SyncManager.syncNow(appContext)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to trigger sync: ${e.message}")
            }
        }
    }

    fun start() {
        try {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback: ${e.message}")
        }
    }

    fun stop() {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister network callback: ${e.message}")
        }
    }
}