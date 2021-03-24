package com.pointlessapps.tvremote_client.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pointlessapps.tvremote_client.App
import com.pointlessapps.tvremote_client.models.DeviceWrapper

class ViewModelDevicePairing(application: Application) : AndroidViewModel(application) {

	private val deviceWrapper = DeviceWrapper((application as App).device)
	private val _secret = MutableLiveData("")
	val secret: LiveData<String>
		get() = _secret

	fun disconnect() {
		deviceWrapper.device!!.disconnect()
	}

	fun setPairingSecret(secret: String) {
		deviceWrapper.device!!.setPairingSecret(secret)
	}

	fun addSymbol(symbol: String) {
		_secret.value = _secret.value?.plus(symbol)
	}

	fun clearSecret() {
		_secret.value = ""
	}
}
