package com.pointlessapps.tvremote_client.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.github.kittinunf.fuel.core.Request
import com.google.android.tv.support.remote.discovery.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun Request.string(callback: (String?) -> Any) =
    responseString { _, _, (body, _) -> callback(body) }

fun Context.saveDeviceInfo(deviceInfo: DeviceInfo?) {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).edit().apply {
        if (deviceInfo == null) {
            remove("deviceInfo")
        } else {
            putString("deviceInfo", deviceInfo.uri.toString())
        }
    }.apply()
}

fun Context.loadDeviceInfo(): DeviceInfo? {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).apply {
        return getString("deviceInfo", null)?.let {
            DeviceInfo.fromUri(Uri.parse(it))
        }
    }
}

fun runAsyncCatch(block: suspend () -> Unit) = runAsyncCatch(block, null)

fun runAsyncCatch(block: suspend () -> Unit, error: (() -> Unit)?) {
    GlobalScope.launch(Dispatchers.IO) {
        runCatching { block() }.exceptionOrNull()?.also {
            error?.invoke()
            Log.e("LOG!", "runAsyncCatch exception: ", it)
        }
    }
}