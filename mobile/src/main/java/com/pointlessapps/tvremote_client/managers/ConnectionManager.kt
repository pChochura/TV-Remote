package com.pointlessapps.tvremote_client.managers

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.google.android.tv.support.remote.core.Device
import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.pointlessapps.tvremote_client.App
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.services.PreferencesService
import com.pointlessapps.tvremote_client.tv.TvRemote
import java.util.*

class ConnectionManager {

	private val handlerThread = HandlerThread("${javaClass.name}.${Calendar.getInstance().timeInMillis}")
	private lateinit var preferencesService: PreferencesService
	private val handler by lazy { Handler(handlerThread.looper) }
	private val deviceWrapper = DeviceWrapper(null)
	val remote = TvRemote(deviceWrapper)

	var onDisconnectedListener: (() -> Unit)? = null

	fun init(application: App) {
		preferencesService = application.preferencesService
		handlerThread.start()
	}

	fun quit() {
		handlerThread.quit()
	}

	fun connect(context: Context, deviceInfo: DeviceInfo) {
		deviceWrapper.device = Device.from(
			context,
			deviceInfo,
			deviceWrapper.deviceListener,
			handler
		)
	}

	fun disconnect() {
		deviceWrapper.device?.disconnect()
	}

	fun setPairingSecret(secret: String) {
		deviceWrapper.device?.setPairingSecret(secret)
	}

	fun setConnectionListener(
		onConnectFailed: (Device) -> Unit,
		onConnecting: (Device) -> Unit,
		onConnected: (Device) -> Unit,
		onDisconnected: (Device) -> Unit,
		onPairingRequired: (Device) -> Unit
	) {
		deviceWrapper.apply {
			setOnConnectFailedListener { onConnectFailed(it) }
			setOnConnectingListener { onConnecting(it) }
			setOnConnectedListener { onConnected(it) }
			setOnDisconnectedListener {
				onDisconnected(it)
				onDisconnectedListener?.invoke()
			}
			setOnPairingRequiredListener { onPairingRequired(it) }
		}
	}

	fun isConnected() = deviceWrapper.device?.isConnected == true
}