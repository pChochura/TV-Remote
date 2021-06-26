package com.pointlessapps.tvremote_client.viewModels

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.google.android.tv.support.remote.discovery.Discoverer
import com.pointlessapps.tvremote_client.App
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.services.ConnectionService
import com.pointlessapps.tvremote_client.utils.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ViewModelDeviceDiscovery(application: Application) : AndroidViewModel(application) {

	private val preferencesService = (application as App).preferencesService
	private val _state = MutableLiveData(State.SEARCHING)
	val state: LiveData<State>
		get() = _state

	private val _devices = MutableLiveData(listOf<DeviceInfo>())
	val devices: LiveData<List<DeviceInfo>>
		get() = _devices

	private lateinit var onGetServiceCallback: () -> ConnectionService
	private val discoverer = Discoverer(application)
	private val handler = Handler(Looper.getMainLooper())
	private val discoveryListener = object : DiscoveryListenerImpl() {
		override fun onStartDiscoveryFailed(errorCode: Int) {
			_state.value = State.FAILED
		}

		override fun onDeviceFound(deviceInfo: DeviceInfo) {
			viewModelScope.launch {
				val devices = _devices.value!!
				if (devices.find { it.uri == deviceInfo.uri } != null) {
					return@launch
				}
				val savedDevice = getSettings().deviceInfo
				if (savedDevice != null && deviceInfo.uri == savedDevice.uri) {
					_state.postValue(State.LOADING)

					return@launch
				}

				_state.postValue(State.FOUND)
				_devices.postValue(listOf(*devices.toTypedArray(), deviceInfo))
			}
		}

		override fun onDeviceLost(deviceInfo: DeviceInfo) {
			val devices = _devices.value!!
			val filteredDevices =
				listOf(*(devices.filterNot { it.uri == deviceInfo.uri }).toTypedArray())
			_devices.value = filteredDevices

			if (filteredDevices.isEmpty()) {
				_state.value = State.NO_DEVICES
			}
		}
	}

	private suspend fun getSettings() = preferencesService.getSettings().first()

	fun setOnGetServiceCallback(onGetServiceCallback: () -> ConnectionService) {
		this.onGetServiceCallback = onGetServiceCallback
		setDeviceListener()
		if (onGetServiceCallback().isConnected()) {
			_state.postValue(State.CONNECTED)
		}
	}

	fun setDeviceListener() {
		if (!::onGetServiceCallback.isInitialized) {
			return
		}

		onGetServiceCallback().setConnectionListener(
			onConnectFailed = { _state.postValue(State.FAILED) },
			onConnecting = { _state.postValue(State.LOADING) },
			onConnected = { _state.postValue(State.CONNECTED) },
			onDisconnected = { startDiscovery() },
			onPairingRequired = { _state.postValue(State.PAIRING) },
		)
	}

	fun loadDeviceInfo() {
		viewModelScope.launch {
			val settings = getSettings()
			if (!settings.openLastConnection) {
				clearSavedDevice()

				return@launch
			}

			loadDevice(settings.deviceInfo ?: return@launch)
		}
	}

	fun stopDiscovery() {
		discoverer.stopDiscovery()
	}

	fun startDiscovery() {
		discoverer.startDiscovery(discoveryListener, handler)
		_state.postValue(
			when {
				_devices.value.isNullOrEmpty() -> State.SEARCHING
				else -> State.FOUND
			}
		)
	}

	fun clearSavedDevice() {
		viewModelScope.launch {
			preferencesService.setSettings(getSettings().also {
				it.deviceInfo = null
			})
		}
	}

	fun disconnect() {
		onGetServiceCallback().disconnect()
	}

	fun loadDevice(deviceInfo: DeviceInfo) {
		if (!::onGetServiceCallback.isInitialized) {
			return
		}

		_state.value = State.LOADING
		onGetServiceCallback().connectIfNecessary(deviceInfo)
		viewModelScope.launch {
			preferencesService.setSettings(getSettings().also {
				it.deviceInfo = deviceInfo
			})
		}
	}

	enum class State(
		val buttonRetryVisible: Boolean = false,
		val imageErrorVisible: Boolean = false,
		val progressVisible: Boolean = false,
		val listDevicesVisible: Boolean = false,
		val textLabelVisible: Boolean = false,
		@StringRes val label: Int? = null
	) {
		LOADING(true, false, true, false, true, R.string.loading),
		SEARCHING(false, false, true, false, true, R.string.searching),
		FAILED(true, true, false, false, true, R.string.something_went_wrong),
		FOUND(false, false, false, true, false),
		NO_DEVICES(true, true, false, false, true, R.string.no_devices_found),
		CONNECTED, PAIRING
	}

}