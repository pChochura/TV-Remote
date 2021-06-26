package com.pointlessapps.tvremote_client.services

import android.app.*
import android.content.ComponentName
import android.content.Intent
import android.os.Binder
import android.service.quicksettings.TileService
import android.view.KeyEvent
import com.google.android.tv.support.remote.core.Device
import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.pointlessapps.tvremote_client.App
import com.pointlessapps.tvremote_client.MainActivity
import com.pointlessapps.tvremote_client.managers.ConnectionManager
import com.pointlessapps.tvremote_client.managers.NotificationManager
import kotlinx.coroutines.*

class ConnectionService : Service() {

	companion object {
		const val DISCONNECT = "disconnect"
	}

	private val coroutineScope = CoroutineScope(Job() + Dispatchers.Default)
	private val binder = ConnectionBinder()
	private val connectionManager = ConnectionManager()

	override fun onCreate() {
		connectionManager.init(application as App)
		coroutineScope.launch {
			while (connectionManager.isConnected()) {
				delay(1000)
				connectionManager.remote.sendClick(KeyEvent.KEYCODE_UNKNOWN)
			}
		}
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		startForeground(1234, NotificationManager.createNotification(applicationContext))
		if (intent?.getBooleanExtra(DISCONNECT, false) == true) {
			quit()

			return START_NOT_STICKY
		}

		return START_STICKY
	}

	fun connectIfNecessary(deviceInfo: DeviceInfo) {
		if (connectionManager.isConnected()) {
			return
		}

		connectionManager.connect(applicationContext, deviceInfo)
		TileService.requestListeningState(
			application,
			ComponentName(application, TvRemoteQTService::class.java)
		)
	}

	fun disconnect() {
		connectionManager.disconnect()
	}

	fun setPairingSecret(secret: String) {
		connectionManager.setPairingSecret(secret)
	}

	fun isConnected() = connectionManager.isConnected()

	fun remote() = connectionManager.remote

	fun setConnectionListener(
		onConnectFailed: (Device) -> Unit,
		onConnecting: (Device) -> Unit,
		onConnected: (Device) -> Unit,
		onDisconnected: (Device) -> Unit,
		onPairingRequired: (Device) -> Unit
	) = connectionManager.setConnectionListener(
		onConnectFailed, onConnecting, onConnected, onDisconnected, onPairingRequired
	)

	fun quit() {
		onDestroy()
		sendBroadcast(Intent(MainActivity.CLOSE_APP))
	}

	override fun onBind(intent: Intent?) = binder

	override fun onDestroy() {
		coroutineScope.launch {
			connectionManager.remote.powerOff()
			withContext(Dispatchers.Main) {
				disconnect()
				stopForeground(true)
				stopSelf()
				TileService.requestListeningState(
					application,
					ComponentName(application, TvRemoteQTService::class.java)
				)
				connectionManager.onDisconnectedListener = {
					connectionManager.quit()
				}
			}
		}
	}

	inner class ConnectionBinder : Binder() {
		val service: ConnectionService
			get() = this@ConnectionService
	}
}