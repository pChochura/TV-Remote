package com.pointlessapps.tvremote_client.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pointlessapps.tvremote_client.App
import kotlinx.coroutines.flow.first

class ViewModelMain(application: Application) : AndroidViewModel(application) {

	private val preferencesService = (application as App).preferencesService

	suspend fun getSettings() = preferencesService.getSettings().first()
}