package com.pointlessapps.tvremote_client.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import com.github.kittinunf.fuel.core.Request
import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.models.Application
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

fun Context.saveTurnTvOn(turnTvOn: Boolean) {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).edit().apply {
        putBoolean("turnTvOn", turnTvOn)
    }.apply()
}

fun Context.loadTurnTvOn(): Boolean {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).apply {
        return getBoolean("turnTvOn", true)
    }
}

fun Context.saveCloseApplication(closeApplication: Boolean) {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).edit().apply {
        putBoolean("closeApplication", closeApplication)
    }.apply()
}

fun Context.loadCloseApplication(): Boolean {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).apply {
        return getBoolean("closeApplication", true)
    }
}

fun Context.saveShowDpad(showDpad: Boolean) {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).edit().apply {
        putBoolean("showDpad", showDpad)
    }.apply()
}

fun Context.loadShowDpad(): Boolean {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).apply {
        return getBoolean("showDpad", false)
    }
}

fun Context.saveShowOnLockScreen(showOnLockScreen: Boolean) {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).edit().apply {
        putBoolean("showOnLockScreen", showOnLockScreen)
    }.apply()
}

fun Context.loadShowOnLockScreen(): Boolean {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).apply {
        return getBoolean("showOnLockScreen", false)
    }
}

fun Context.saveOpenLastConnection(openLastConnection: Boolean) {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).edit().apply {
        putBoolean("openLastConnection", openLastConnection)
    }.apply()
}

fun Context.loadOpenLastConnection(): Boolean {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).apply {
        return getBoolean("openLastConnection", true)
    }
}

fun Context.saveShortcuts(shortcuts: List<Application>) {
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).edit().apply {
        putString("shortcuts", Gson().toJson(shortcuts))
    }.apply()
}

fun Context.loadShortcuts(): List<Application> {
    val defaultApplications = listOf(
        Application(R.mipmap.app_netflix, "com.netflix.ninja", "com.netflix.ninja.MainActivity"),
        Application(R.mipmap.app_tidal, "com.aspiro.tidal", "com.aspiro.wamp.LoginFragmentActivity"),
        Application(R.mipmap.app_youtube, "com.google.android.youtube.tv", "com.google.android.apps.youtube.tv.activity.ShellActivity"),
        Application(R.mipmap.app_spotify, "com.spotify.tv.android", "com.spotify.tv.android.SpotifyTVActivity"),
        Application(R.mipmap.app_cda, "pl.cda.tv", "pl.cda.tv.ui.welcome.WelcomeActivity"),
        Application(R.mipmap.app_play_store, "com.android.vending", "com.google.android.finsky.tvmainactivity.TvMainActivity"),
    )
    getSharedPreferences("tv_remote_prefs", Context.MODE_PRIVATE).apply {
        return Gson().fromJson(
            getString("shortcuts", null) ?: return defaultApplications,
            object : TypeToken<List<Application>>() {}.type
        )
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

fun View.scaleAnimation() {
    arrayOf("scaleX", "scaleY").forEach {
        ObjectAnimator.ofFloat(this, it, 0.9f, 1.1f, 1.0f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            duration = 2000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
}