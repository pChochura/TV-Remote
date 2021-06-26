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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModelRemote(application: Application) : AndroidViewModel(application) {

	private lateinit var onGetServiceCallback: () -> ConnectionService
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

	suspend fun getSettings() = preferencesService.getSettings().first()

	fun setOnGetServiceCallback(onGetServiceCallback: () -> ConnectionService) {
		this.onGetServiceCallback = onGetServiceCallback
	}

	fun setDeviceListener() {
		if (!::onGetServiceCallback.isInitialized) {
			return
		}

		onGetServiceCallback().setOnVoiceListener(
			onStartVoice = { _isVoiceRecording.postValue(true) },
			onStopVoice = { _isVoiceRecording.postValue(false) },
		)

		onGetServiceCallback().setConnectionListener(
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
				if (!onGetServiceCallback().isConnected()) {
					it.cancelPairing()
				}
			},
		)
	}

	fun checkConnectionState() {
		if (::onGetServiceCallback.isInitialized && onGetServiceCallback().isConnected()) {
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

	fun disconnect() {
		onGetServiceCallback().disconnect()
	}

	fun setOnShowImeListener(listener: (EditorInfo?, ExtractedText?) -> Unit) {
		onGetServiceCallback().setOnShowImeListener { info, text ->
			viewModelScope.launch(Dispatchers.Main) { listener(info, text) }
		}
	}

	fun setOnHideImeListener(listener: () -> Unit) {
		onGetServiceCallback().setOnHideImeListener {
			viewModelScope.launch(Dispatchers.Main) { listener() }
		}
	}

	fun setComposingRegion(from: Int, to: Int) {
		onGetServiceCallback().device()?.setComposingRegion(from, to)
	}

	fun setComposingText(text: String, position: Int) {
		onGetServiceCallback().device()?.setComposingText(text, position)
	}

	fun beginBatchEdit() {
		onGetServiceCallback().device()?.beginBatchEdit()
	}

	fun endBatchEdit() {
		onGetServiceCallback().device()?.endBatchEdit()
	}

	fun performEditAction(actionId: Int) {
		onGetServiceCallback().device()?.performEditorAction(actionId)
	}

	fun stopVoiceRecording() {
		onGetServiceCallback().device()?.stopVoice()
		_isVoiceRecording.value = false
	}

	fun sendKeyEvent(keyCode: Int, action: Int) =
		onGetServiceCallback().remote().sendKeyEvent(keyCode, action)

	fun sendClick(keyCode: Int) = onGetServiceCallback().remote().sendClick(keyCode)
	fun sendIntent(intent: Intent) = onGetServiceCallback().remote().sendIntent(intent)

	fun sendLongClick(keyCode: Int) {
		viewModelScope.launch { onGetServiceCallback().remote().sendLongClick(keyCode) }
	}

	fun powerOnIfNecessary() {
		viewModelScope.launch {
			if (getSettings().turnOnTv) {
				onGetServiceCallback().remote().powerOn()
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
		viewModelScope.launch {
			onGetServiceCallback().remote().powerOff()
			TileService.requestListeningState(
				getApplication(),
				ComponentName(getApplication(), TvRemoteQTService::class.java)
			)
			if (getSettings().closeApplication) {
				onGetServiceCallback().disconnect()
				onCloseApplicationListener()
			}
		}
	}

	fun openApp(packageName: String, activityName: String) {
		viewModelScope.launch { onGetServiceCallback().remote().openApp(packageName, activityName) }
	}

	fun vibrateIfEnabled() {
		viewModelScope.launch {
			val settings = getSettings()
			if (!settings.vibrationEnabled || !onGetServiceCallback().isConnected()) {
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