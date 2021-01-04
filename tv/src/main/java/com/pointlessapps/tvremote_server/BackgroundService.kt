package com.pointlessapps.tvremote_server

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.koushikdutta.async.http.server.AsyncHttpServer

class BackgroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        AsyncHttpServer().apply {
            get("/") { _, response ->
                response.send("Netflix is being opened")
                Handler(Looper.getMainLooper()).post {
                    startActivity(packageManager.getLaunchIntentForPackage("com.netflix.ninja"))
                }
            }
        }.listen(8080)
    }
}
