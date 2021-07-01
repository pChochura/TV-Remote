package com.pointlessapps.tvremote_client.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pointlessapps.tvremote_client.App
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.services.ConnectionService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ViewModelDevicePairing(application: Application) : AndroidViewModel(application) {

	private lateinit var onGetServiceCallback: () -> ConnectionService
	private val preferencesService = (application as App).preferencesService
	private val _secret = MutableLiveData("")
	val secret: LiveData<String>
		get() = _secret

	fun setOnGetServiceCallback(onGetServiceCallback: () -> ConnectionService) {
		this.onGetServiceCallback = onGetServiceCallback
	}

	fun disconnect(onDisconnected: () -> Unit) {
		onGetServiceCallback().disconnect(onDisconnected)
	}

	fun setPairingSecret(secret: String) {
		onGetServiceCallback().setPairingSecret(secret)
	}

	fun addSymbol(symbol: String) {
		_secret.value = _secret.value?.plus(symbol)
	}

	fun clearSecret() {
		_secret.value = ""
	}

	fun forgetDevice() {
		viewModelScope.launch {
			preferencesService.setSettings(
				preferencesService.getSettings().first().also {
					it.deviceInfo = null
				}
			)
		}
	}
}
