package com.pointlessapps.tvremote_client.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pointlessapps.tvremote_client.App
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ViewModelSettings(application: Application) : AndroidViewModel(application) {

	private val preferencesService = (application as App).preferencesService

	val settings = preferencesService.getSettings().filterNotNull().asLiveData()
	val shortcuts = preferencesService.getSettings().map { it.shortcuts }.asLiveData()

	fun setTurnOnTv(turnOnTv: Boolean) {
		viewModelScope.launch {
			preferencesService.setSettings(settings.value?.also {
				it.turnOnTv = turnOnTv
			} ?: return@launch)
		}
	}

	fun setCloseApplication(closeApplication: Boolean) {
		viewModelScope.launch {
			preferencesService.setSettings(settings.value?.also {
				it.closeApplication = closeApplication
			} ?: return@launch)
		}
	}

	fun setShowDpad(showDpad: Boolean) {
		viewModelScope.launch {
			preferencesService.setSettings(settings.value?.also {
				it.showDpad = showDpad
			} ?: return@launch)
		}
	}

	fun setShowOnLockScreen(showOnLockScreen: Boolean) {
		viewModelScope.launch {
			preferencesService.setSettings(settings.value?.also {
				it.showOnLockScreen = showOnLockScreen
			} ?: return@launch)
		}
	}

	fun setOpenLastConnection(openLastConnection: Boolean) {
		viewModelScope.launch {
			preferencesService.setSettings(settings.value?.also {
				it.openLastConnection = openLastConnection
			} ?: return@launch)
		}
	}

	fun setVibrationEnabled(vibrationEnabled: Boolean) {
		viewModelScope.launch {
			preferencesService.setSettings(settings.value?.also {
				it.vibrationEnabled = vibrationEnabled
			} ?: return@launch)
		}
	}

	fun addShortcut(shortcut: com.pointlessapps.tvremote_client.models.Application) {
		viewModelScope.launch {
			preferencesService.setSettings(settings.value?.also {
				it.shortcuts = listOf(*it.shortcuts.toTypedArray(), shortcut)
			} ?: return@launch)
		}
	}

	fun updateShortcut(shortcut: com.pointlessapps.tvremote_client.models.Application) {
		viewModelScope.launch {
			preferencesService.setSettings(settings.value?.also { settings ->
				settings.shortcuts = listOf(
					*settings.shortcuts.filter { it.id != shortcut.id }.toTypedArray(),
					shortcut
				)
			} ?: return@launch)
		}
	}

	fun removeShortcut(shortcut: com.pointlessapps.tvremote_client.models.Application) {
		viewModelScope.launch {
			preferencesService.setSettings(settings.value?.also { settings ->
				settings.shortcuts = settings.shortcuts.filter { it.id != shortcut.id }
			} ?: return@launch)
		}
	}

	fun setShortcuts(shortcuts: List<com.pointlessapps.tvremote_client.models.Application>) {
		viewModelScope.launch {
			preferencesService.setSettings(settings.value?.also {
				it.shortcuts = shortcuts
			} ?: return@launch)
		}
	}
}