package com.pointlessapps.tvremote_client.tv

import android.content.Intent
import android.view.KeyEvent
import com.github.kittinunf.fuel.core.awaitUnit
import com.github.kittinunf.fuel.httpPost
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.utils.SwipeTouchHandler
import kotlinx.coroutines.delay

class TvRemote(private val deviceWrapper: DeviceWrapper) {

	fun disconnect() = deviceWrapper.device?.disconnect()

	fun sendKeyEvent(keyCode: Int, action: Int) {
		deviceWrapper.device?.sendKeyEvent(keyCode, action)
	}

	fun sendClick(keyCode: Int) {
		sendKeyEvent(keyCode, KeyEvent.ACTION_DOWN)
		sendKeyEvent(keyCode, KeyEvent.ACTION_UP)
	}

	suspend fun sendLongClick(keyCode: Int) {
		sendKeyEvent(keyCode, KeyEvent.ACTION_DOWN)
		delay(SwipeTouchHandler.LONG_CLICK_TIME)
		sendKeyEvent(keyCode, KeyEvent.ACTION_UP)
	}

	fun sendIntent(intent: Intent) {
		deviceWrapper.device?.sendIntent(intent)
	}

	suspend fun powerOn() {
		runCatching {
			"http://${deviceWrapper.device?.deviceInfo?.uri?.host}:8080/power/on"
				.httpPost().awaitUnit()
		}.onFailure { sendClick(KeyEvent.KEYCODE_POWER) }
	}

	suspend fun powerOff() {
		runCatching {
			println("LOG!, turning off. Device info: ${deviceWrapper.device?.deviceInfo}")
			"http://${deviceWrapper.device?.deviceInfo?.uri?.host}:8080/power/off"
				.httpPost().awaitUnit()
		}.onFailure {
			println("LOG!, error: $it")
			sendClick(KeyEvent.KEYCODE_POWER)
		}
	}

	suspend fun openApp(packageName: String, activityName: String) {
		runCatching {
			println("LOG!, package: $packageName")
			"http://${deviceWrapper.device?.deviceInfo?.uri?.host}:8080/open/${packageName}"
				.httpPost().awaitUnit()
		}.onFailure {
			sendIntent(Intent(Intent.ACTION_MAIN).apply {
				setClassName(packageName, activityName)
			})
		}
	}
}