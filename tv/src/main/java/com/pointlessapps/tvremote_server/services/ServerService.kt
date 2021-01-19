package com.pointlessapps.tvremote_server.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.pointlessapps.tvremote_server.R
import com.pointlessapps.tvremote_server.tv.TvAccessories
import com.pointlessapps.tvremote_server.tv.TvRemote
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray

@RequiresApi(Build.VERSION_CODES.O)
class ServerService : Service() {

    companion object {
        private const val PORT = 8080
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var serviceStarted = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) =
        START_STICKY.also { startService() }

    @SuppressLint("WakelockTimeout")
    private fun startService() {
        if (serviceStarted) {
            return
        }

        serviceStarted = true
        wakeLock =
            (applicationContext.getSystemService(PowerManager::class.java)).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TvRemoteServer::lock").apply {
                    acquire()
                }
            }

        AsyncHttpServer().apply {
            get("/power") { _, res ->
                res.send(TvRemote.isPoweredOn(applicationContext).toString())
            }
            post("/power") { _, res ->
                res.send(TvRemote.togglePower(applicationContext).toString())
            }
            post("/power/on") { _, res ->
                TvRemote.powerOn(applicationContext)
                res.send("")
            }
            post("/power/off") { _, res ->
                TvRemote.powerOff(applicationContext)
                res.send("")
            }
            get("/bt/list") { _, res ->
                res.send(JSONArray(TvAccessories.getBtAccessories(applicationContext)))
            }
            post("/bt/discover") { _, res ->
                TvAccessories.startBtDiscovery(applicationContext)
                res.send("")
            }
            post("/bt/(dis)?connect/([A-Fa-f0-9]{2}(?>:[A-Fa-f0-9]{2}){5})") { req, res ->
                val address = req.matcher.group(2)
                if (address == null) {
                    res.code(400).send("")

                    return@post
                }

                when (req.matcher.group(1)) {
                    "dis" -> TvAccessories.disconnectBtDevice(applicationContext, address)
                    else -> TvAccessories.connectBtDevice(applicationContext, address)
                }

                res.send("")
            }
            post("/input") { _, res ->
                TvRemote.showSourceInput(applicationContext)
                res.send("")
            }
            get("/apps") { _, res ->
                res.send(JSONArray(TvRemote.getApplicationList(applicationContext)))
            }
            post("/open/(.+)") { req, res ->
                TvRemote.openApplication(applicationContext, req.matcher.group(1) ?: "")
                res.send("")
            }
        }.listen(PORT)

        GlobalScope.launch {
            while (serviceStarted) {
                delay(60000)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground()
        }
    }

    override fun onDestroy() {
        wakeLock?.takeIf { it.isHeld }?.release()
        stopSelf()
        stopForeground(true)
        serviceStarted = false
    }

    private fun startForeground() {
        NotificationChannel(
            "tv_remote_notifications",
            "TV Remote notifications",
            NotificationManager.IMPORTANCE_NONE
        ).also {
            applicationContext.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(it)
        }

        val notification: Notification = Notification.Builder(this, "tv_remote_notifications")
            .setSmallIcon(R.drawable.ic_remote)
            .setContentText("Running tv remote server")
            .setContentTitle("TV Remote - server")
            .setOngoing(true)
            .setAutoCancel(false)
            .setLocalOnly(true)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}