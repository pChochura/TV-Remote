package com.pointlessapps.tvremote_client

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.pointlessapps.tvremote_client.databinding.ActivityMainBinding
import com.pointlessapps.tvremote_client.fragments.FragmentBase
import com.pointlessapps.tvremote_client.services.ConnectionService

class MainActivity : AppCompatActivity() {

	companion object {
		const val DESTINATION = "destination"
		const val CLOSE_APP = "close_app"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setTheme(R.style.AppTheme)

		ContextCompat.startForegroundService(
			applicationContext,
			Intent(applicationContext, ConnectionService::class.java)
		)

		val binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		registerReceiver(object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) = finish()
		}, IntentFilter(CLOSE_APP))

		intent.getIntExtra(DESTINATION, 0).also {
			if (it == 0) {
				return@also
			}

			findNavController(R.id.fragmentContainer).navigate(it)
		}
	}

	override fun dispatchKeyEvent(event: KeyEvent) =
		(supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.childFragmentManager?.fragments?.firstOrNull() as? FragmentBase<*>)
			?.onDispatchKeyEvent?.invoke(event) ?: super.dispatchKeyEvent(event)
}