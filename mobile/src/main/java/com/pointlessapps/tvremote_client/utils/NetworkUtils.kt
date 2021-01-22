package com.pointlessapps.tvremote_client.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper

object NetworkUtils {

    fun registerNetworkChangeListener(context: Context, onChanged: (Boolean) -> Unit) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val networkCallback: ConnectivityManager.NetworkCallback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) = onChanged.invoke(false)
                override fun onAvailable(network: Network) = onChanged.invoke(true)
            }

        connectivityManager.registerDefaultNetworkCallback(networkCallback, Handler(Looper.getMainLooper()))
    }
}