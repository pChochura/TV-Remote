package com.pointlessapps.tvremote_server.utils

import android.content.Context
import android.content.SharedPreferences

private const val name = "TV_REMOTE_ACCESSORIES_KEY"

fun Context.setDeviceConnected(address: String, connected: Boolean = true) {
    getPreferences(this).edit().also {
        it.putBoolean(address, connected)
        it.apply()
    }
}

fun Context.isDeviceConnected(address: String) = getPreferences(this).getBoolean(address, false)

private fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(name, Context.MODE_PRIVATE)
}