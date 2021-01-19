package com.pointlessapps.tvremote_server.services

import android.content.Intent
import android.graphics.Color
import android.service.dreams.DreamService
import android.view.View
import com.pointlessapps.tvremote_server.tv.TvRemote

class DreamingService : DreamService() {

    override fun onUnbind(intent: Intent?): Boolean {
        if (intent?.getBooleanExtra("STOP_DREAMING", false) == true) {
            wakeUp()
        }

        return super.onUnbind(intent)
    }

    override fun onAttachedToWindow() {
        isInteractive = true
        isFullscreen = true
        isScreenBright = false

        setContentView(View(applicationContext).apply {
            setBackgroundColor(Color.BLACK)
        })

        TvRemote.powerOff(applicationContext)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        TvRemote.powerOn(applicationContext)
    }
}