package com.pointlessapps.tvremote_server.tv

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import com.pointlessapps.tvremote_server.services.DreamingService
import org.json.JSONObject


object TvRemote {

    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build()

    fun powerOn(context: Context) {
        togglePower(context, false)
        releaseAudioFocus(context)
        stopDreamService(context)
    }

    private fun stopDreamService(context: Context) {
        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) = Unit
            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }
        context.bindService(
            Intent(context, DreamingService::class.java).putExtra("STOP_DREAMING", true),
            conn,
            Context.BIND_IMPORTANT
        )
        context.unbindService(conn)
    }

    private fun releaseAudioFocus(context: Context) {
        val audioManager = context.getSystemService(AudioManager::class.java)
        audioManager.abandonAudioFocusRequest(focusRequest!!)
    }

    fun powerOff(context: Context) {
        togglePower(context, true)
        gainAudioFocus(context)
        startDreamService(context)
    }

    private fun startDreamService(context: Context) {
        context.startActivity(Intent(Intent.ACTION_MAIN).apply {
            setClassName("com.android.systemui", "com.android.systemui.Somnambulator")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun gainAudioFocus(context: Context) {
        val audioManager = context.getSystemService(AudioManager::class.java)
        if (audioManager.isMusicActive) {
            val mediaIntent = Intent("com.android.music.musicservicecommand")
            mediaIntent.putExtra("command", "pause")
            context.sendBroadcast(mediaIntent)
        }

        audioManager.requestAudioFocus(focusRequest!!)
    }

    fun togglePower(context: Context): Boolean? {
        val backlight = isPoweredOn(context) ?: return null

        if (backlight) {
            powerOff(context)
        } else {
            powerOn(context)
        }

        return !backlight
    }

    private fun togglePower(context: Context, powerOff: Boolean) {
        Class.forName("com.tcl.tvmanager.TTvFunctionManager").apply {
            val newInstance = getMethod("getInstance", Context::class.java).invoke(null, context)
            getMethod("setPowerBacklight", Boolean::class.java).invoke(newInstance, !powerOff)
        }
    }

    fun isPoweredOn(context: Context) =
        Class.forName("com.tcl.tvmanager.TTvFunctionManager").let {
            val newInstance = it.getMethod("getInstance", Context::class.java).invoke(null, context)
            it.getMethod("getPowerBacklightSate").invoke(newInstance) as? Boolean
        }

    fun showSourceInput(context: Context) =
        context.startActivity(Intent("com.android.tv.action.VIEW_INPUTS").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            setClassName("com.tcl.sourcemananger", "com.tcl.sourcemanager.MainActivity")
        })

    fun getApplicationList(context: Context): List<JSONObject> =
        context.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).also {
                it.addCategory(Intent.CATEGORY_LAUNCHER)
                it.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            },
            PackageManager.MATCH_ALL
        ).map {
            JSONObject(
                """
                {
                    "name": "${context.packageManager.getApplicationLabel(it.activityInfo.applicationInfo)}",
                    "packageName": "${it.activityInfo.packageName}"
                }
                """.trimIndent()
            )
        }

    fun openApplication(context: Context, packageName: String) = try {
        context.startActivity(
            context.packageManager.getLeanbackLaunchIntentForPackage(
                packageName
            )
        )
    } catch (ignored: Exception) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(packageName))
    }
}