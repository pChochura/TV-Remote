package com.pointlessapps.tvremote_client.receivers

import android.content.*
import android.os.IBinder
import com.pointlessapps.tvremote_client.services.TvRemoteQTService

class QtStateRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in arrayOf(
                TvRemoteQTService.ACTION_TOGGLE_POWER,
                TvRemoteQTService.ACTION_REFRESH_STATE
            )
        ) {
            val serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) = Unit
                override fun onServiceDisconnected(name: ComponentName?) = Unit
            }
            context.applicationContext.bindService(
                Intent(context, TvRemoteQTService::class.java).apply {
                    action = intent.action
                    putExtra("state", intent.getStringExtra("state"))
                },
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
            context.applicationContext.unbindService(serviceConnection)
        }
    }

    companion object {
        fun sendBroadcast(context: Context, action: String, state: String = "") {
            context.sendBroadcast(
                Intent(action).setClass(
                    context,
                    QtStateRefreshReceiver::class.java
                ).putExtra("state", state)
            )
        }
    }
}