package com.pointlessapps.tvremote_client.services

import android.app.*
import android.content.ComponentName
import android.content.Intent
import android.os.Binder
import android.service.quicksettings.TileService
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
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
				delay(5000)
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

	fun disconnect(onDisconnected: (() -> Unit)? = null) {
		connectionManager.disconnect {
			coroutineScope.launch(Dispatchers.Main) {
				onDisconnected?.invoke()
			}
		}
	}

	fun setPairingSecret(secret: String) {
		connectionManager.setPairingSecret(secret)
	}

	fun isConnected() = connectionManager.isConnected()
	fun remote() = connectionManager.remote
	fun device() = connectionManager.device

	fun setConnectionListener(
		onConnectFailed: (Device) -> Unit,
		onConnecting: (Device) -> Unit,
		onConnected: (Device) -> Unit,
		onDisconnected: (Device) -> Unit,
		onPairingRequired: (Device) -> Unit
	) = connectionManager.setConnectionListener(
		onConnectFailed, onConnecting, onConnected, onDisconnected, onPairingRequired
	)

	fun setOnVoiceListener(onStartVoice: (Device) -> Unit, onStopVoice: (Device) -> Unit) {
		connectionManager.setOnVoiceListener(onStartVoice, onStopVoice)
	}

	fun setOnShowImeListener(onShowImeListener: (EditorInfo?, ExtractedText?) -> Unit) {
		connectionManager.setOnShowImeListener(onShowImeListener)
	}

	fun setOnHideImeListener(onHideImeListener: () -> Unit) {
		connectionManager.setOnHideImeListener(onHideImeListener)
	}

	fun quit() {
		onDestroy()
		sendBroadcast(Intent(MainActivity.CLOSE_APP))
	}

	override fun onBind(intent: Intent?) = binder

	override fun onDestroy() {
		coroutineScope.launch(Dispatchers.IO) {
			connectionManager.remote.powerOff()
			withContext(Dispatchers.Main) {
				disconnect { connectionManager.quit() }
				stopForeground(true)
				stopSelf()
				TileService.requestListeningState(
					application,
					ComponentName(application, TvRemoteQTService::class.java)
				)
			}
		}
	}

	inner class ConnectionBinder : Binder() {
		val service: ConnectionService
			get() = this@ConnectionService
	}
}