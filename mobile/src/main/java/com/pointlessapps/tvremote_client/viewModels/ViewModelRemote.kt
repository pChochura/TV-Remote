package com.pointlessapps.tvremote_client.viewModels

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.TileService
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import androidx.lifecycle.*
import com.google.android.tv.support.remote.core.Device
import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.pointlessapps.tvremote_client.App
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.services.TvRemoteQTService
import com.pointlessapps.tvremote_client.tv.TvRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ViewModelRemote(application: Application) : AndroidViewModel(application) {

	private val preferencesService = (application as App).preferencesService
	private val deviceWrapper = DeviceWrapper((application as App).device)
	private val remote = TvRemote(deviceWrapper)

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

	init {
		deviceWrapper.setOnDisconnectedListener {
			_isLoading.value = true
			reconnect()
		}
		deviceWrapper.setOnConnectedListener {
			_isLoading.value = false
			deviceWrapper.device?.beginBatchEdit()
		}
		deviceWrapper.setOnStartVoiceListener { _isVoiceRecording.value = true }
		deviceWrapper.setOnStopVoiceListener { _isVoiceRecording.value = false }
	}

	fun setDeviceInfo(deviceInfo: DeviceInfo?) {
		viewModelScope.launch {
			preferencesService.setSettings(
				getSettings().also { it.deviceInfo = deviceInfo }
			)
		}
	}

	fun reconnect() {
		deviceWrapper.device = Device.from(
			getApplication(),
			deviceWrapper.device!!.deviceInfo,
			deviceWrapper.deviceListener,
			Handler(Looper.getMainLooper())
		)
	}

	fun disconnect() {
		deviceWrapper.device?.disconnect()
	}

	fun setOnShowImeListener(listener: (EditorInfo, ExtractedText?) -> Unit) {
		deviceWrapper.setOnShowImeListener { _, info, text -> listener(info, text) }
	}

	fun setOnHideImeListener(listener: () -> Unit) {
		deviceWrapper.setOnHideImeListener { listener() }
	}

	fun setComposingRegion(from: Int, to: Int) {
		deviceWrapper.device?.setComposingRegion(from, to)
	}

	fun setComposingText(text: String, position: Int) {
		deviceWrapper.device?.setComposingText(text, position)
	}

	fun beginBatchEdit() {
		deviceWrapper.device?.beginBatchEdit()
	}

	fun endBatchEdit() {
		deviceWrapper.device?.endBatchEdit()
	}

	fun performEditAction(actionId: Int) {
		deviceWrapper.device?.performEditorAction(actionId)
	}

	fun stopVoiceRecording() {
		deviceWrapper.device?.stopVoice()
		_isVoiceRecording.value = false
	}

	fun sendKeyEvent(keyCode: Int, action: Int) = remote.sendKeyEvent(keyCode, action)
	fun sendClick(keyCode: Int) = remote.sendClick(keyCode)
	fun sendIntent(intent: Intent) = remote.sendIntent(intent)

	fun sendLongClick(keyCode: Int) {
		viewModelScope.launch { remote.sendLongClick(keyCode) }
	}

	fun powerOn() {
		viewModelScope.launch(Dispatchers.Default) { remote.powerOn() }
		TileService.requestListeningState(
			getApplication(),
			ComponentName(getApplication(), TvRemoteQTService::class.java)
		)
	}

	fun powerOff(onCloseApplicationListener: () -> Unit) {
		viewModelScope.launch(Dispatchers.Default) {
			remote.powerOff()
			TileService.requestListeningState(
				getApplication(),
				ComponentName(getApplication(), TvRemoteQTService::class.java)
			)
			if (getSettings().closeApplication) {
				remote.disconnect()
				onCloseApplicationListener()
			}
		}
	}

	fun openApp(packageName: String, activityName: String) {
		viewModelScope.launch(Dispatchers.Default) { remote.openApp(packageName, activityName) }
	}
}