package com.pointlessapps.tvremote_client.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.models.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class PreferencesService(coroutineScope: CoroutineScope, private val context: Context) {

	companion object {
		const val KEY_SETTINGS = "settings"
	}

	private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
		name = "${
			context.getString(
				R.string.app_name
			).toLowerCase(Locale.getDefault())
		}_prefs",
		scope = coroutineScope
	)

	fun getSettings() = context.dataStore.data.map {
		Json.decodeFromString<Settings>(it[stringPreferencesKey(KEY_SETTINGS)] ?: return@map Settings())
	}

	suspend fun setSettings(settings: Settings) {
		context.dataStore.edit {
			it[stringPreferencesKey(KEY_SETTINGS)] = Json.encodeToString(settings)
		}
	}
}