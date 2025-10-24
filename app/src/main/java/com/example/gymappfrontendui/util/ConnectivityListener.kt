package com.example.gymappfrontendui.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log

class ConnectivityListener(
    private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d("ConnectivityListener", "Internet connection restored. Starting sync...")
            SyncManager.syncNow(context)
        }
    }

    fun start() {
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    fun stop() {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}