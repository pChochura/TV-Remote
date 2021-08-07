package com.pointlessapps.tvremote_client

import android.app.Application
import com.pointlessapps.tvremote_client.services.PreferencesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class App : Application() {

	private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
	val preferencesService: PreferencesService by lazy {
		PreferencesService(
			coroutineScope,
			applicationContext
		)
	}
}