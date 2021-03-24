package com.pointlessapps.tvremote_client.models

import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.serializers.SerializerDeviceInfo
import kotlinx.serialization.Serializable

@Serializable
class Settings(
	@Serializable(with = SerializerDeviceInfo::class)
	var deviceInfo: DeviceInfo? = null,
	var turnOnTv: Boolean = true,
	var closeApplication: Boolean = true,
	var showDpad: Boolean = false,
	var showOnLockScreen: Boolean = true,
	var openLastConnection: Boolean = true,
	var shortcuts: List<Application> = defaultApplications
) {

	companion object {
		val defaultApplications = listOf(
			Application(
				R.mipmap.app_netflix,
				"com.netflix.ninja",
				"com.netflix.ninja.MainActivity"
			),
			Application(
				R.mipmap.app_tidal,
				"com.aspiro.tidal",
				"com.aspiro.wamp.LoginFragmentActivity"
			),
			Application(
				R.mipmap.app_youtube,
				"com.google.android.youtube.tv",
				"com.google.android.apps.youtube.tv.activity.ShellActivity"
			),
			Application(
				R.mipmap.app_spotify,
				"com.spotify.tv.android",
				"com.spotify.tv.android.SpotifyTVActivity"
			),
			Application(R.mipmap.app_cda, "pl.cda.tv", "pl.cda.tv.ui.welcome.WelcomeActivity"),
			Application(
				R.mipmap.app_play_store,
				"com.android.vending",
				"com.google.android.finsky.tvmainactivity.TvMainActivity"
			),
		)
	}
}