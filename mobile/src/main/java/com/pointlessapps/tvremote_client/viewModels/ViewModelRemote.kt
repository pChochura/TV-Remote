package com.pointlessapps.tvremote_client.viewModels

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.service.quicksettings.TileService
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import androidx.lifecycle.*
import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.pointlessapps.tvremote_client.App
import com.pointlessapps.tvremote_client.services.ConnectionService
import com.pointlessapps.tvremote_client.services.TvRemoteQTService
import com.pointlessapps.tvremote_client.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModelRemote(application: Application) : AndroidViewModel(application) {

	private var onGetServiceCallback: (() -> ConnectionService)? = null
	private val preferencesService = (application as App).preferencesService

	val checkedShortcuts = preferencesService.getSettings()
		.map { settings -> settings.shortcuts.filter { it.checked } }
		.asLiveData()

	private val _isLoading = MutableLiveData(true)
	val isLoading: LiveData<Boolean>
		get() = _isLoading

	private val _isVoiceRecording = MutableLiveData(false)
	val isVoiceRecording: LiveData<Boolean>
		get() = _isVoiceRecording

	init {
		viewModelScope.launch {
			delay(5000)
			_isLoading.postValue(onGetServiceCallback?.invoke()?.isConnected() != true)
		}
	}

	suspend fun getSettings() = preferencesService.getSettings().first()

	private fun getDevice() = onGetServiceCallback?.invoke()?.device()
	private fun getRemote() = onGetServiceCallback?.invoke()?.remote()

	fun setOnGetServiceCallback(onGetServiceCallback: () -> ConnectionService) {
		this.onGetServiceCallback = onGetServiceCallback
	}

	fun setDeviceListener() {
		onGetServiceCallback?.invoke()?.setOnVoiceListener(
			onStartVoice = { _isVoiceRecording.postValue(true) },
			onStopVoice = { _isVoiceRecording.postValue(false) },
		)

		onGetServiceCallback?.invoke()?.setConnectionListener(
			onConnectFailed = {},
			onConnecting = {},
			onConnected = {
				_isLoading.postValue(false)
				it.beginBatchEdit()
			},
			onDisconnected = {
				_isLoading.postValue(true)
			},
			onPairingRequired = {
				if (onGetServiceCallback?.invoke()?.isConnected() != true) {
					it.cancelPairing()
				}
			},
		)
	}

	fun checkConnectionState() {
		if (onGetServiceCallback?.invoke()?.isConnected() == true) {
			_isLoading.postValue(false)
		}
	}

	fun setDeviceInfo(deviceInfo: DeviceInfo?) {
		viewModelScope.launch {
			preferencesService.setSettings(
				getSettings().also { it.deviceInfo = deviceInfo }
			)
		}
	}

	fun disconnect(onDisconnected: () -> Unit) {
		onGetServiceCallback?.invoke()?.disconnect(onDisconnected)
	}

	fun setOnShowImeListener(listener: (EditorInfo?, ExtractedText?) -> Unit) {
		onGetServiceCallback?.invoke()?.setOnShowImeListener { info, text ->
			viewModelScope.launch(Dispatchers.Main) { listener(info, text) }
		}
	}

	fun setOnHideImeListener(listener: () -> Unit) {
		onGetServiceCallback?.invoke()?.setOnHideImeListener {
			viewModelScope.launch(Dispatchers.Main) { listener() }
		}
	}

	fun setComposingRegion(from: Int, to: Int) {
		getDevice()?.setComposingRegion(from, to)
	}

	fun setComposingText(text: String, position: Int) {
		getDevice()?.setComposingText(text, position)
	}

	fun beginBatchEdit() {
		getDevice()?.beginBatchEdit()
	}

	fun endBatchEdit() {
		getDevice()?.endBatchEdit()
	}

	fun performEditAction(actionId: Int) {
		getDevice()?.performEditorAction(actionId)
	}

	fun stopVoiceRecording() {
		getDevice()?.stopVoice()
		_isVoiceRecording.value = false
	}

	fun sendKeyEvent(keyCode: Int, action: Int) = getRemote()?.sendKeyEvent(keyCode, action)
	fun sendClick(keyCode: Int) = getRemote()?.sendClick(keyCode)
	fun sendIntent(intent: Intent) = getRemote()?.sendIntent(intent)

	fun sendLongClick(keyCode: Int) {
		viewModelScope.launch { getRemote()?.sendLongClick(keyCode) }
	}

	fun powerOnIfNecessary() {
		viewModelScope.launch(Dispatchers.IO) {
			if (getSettings().turnOnTv) {
				getRemote()?.powerOn()
			}

			withContext(Dispatchers.Main) {
				TileService.requestListeningState(
					getApplication(),
					ComponentName(getApplication(), TvRemoteQTService::class.java)
				)
			}
		}
	}

	fun powerOff(onCloseApplicationListener: () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			getRemote()?.powerOff()
			TileService.requestListeningState(
				getApplication(),
				ComponentName(getApplication(), TvRemoteQTService::class.java)
			)
			if (getSettings().closeApplication) {
				onGetServiceCallback?.invoke()?.disconnect()
				onCloseApplicationListener()
			}
		}
	}

	fun openApp(packageName: String, activityName: String) {
		viewModelScope.launch(Dispatchers.IO) {
			getRemote()?.openApp(packageName, activityName)
		}
	}

	fun vibrateIfEnabled() {
		viewModelScope.launch {
			val settings = getSettings()
			if (!settings.vibrationEnabled || onGetServiceCallback?.invoke()
					?.isConnected() != true
			) {
				return@launch
			}

			Utils.vibrate(getApplication())
		}
	}

	fun toggleShowOnLockScreenIfEnabled(activity: Activity) {
		viewModelScope.launch {
			if (getSettings().showOnLockScreen) {
				Utils.toggleShowOnLockScreen(activity, true)
			}
		}
	}
}