package com.pointlessapps.tvremote_client

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pointlessapps.tvremote_client.databinding.ActivityMainBinding
import com.pointlessapps.tvremote_client.fragments.FragmentBase
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelMain

class MainActivity : AppCompatActivity() {

	private val viewModel by viewModels<ViewModelMain>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setTheme(R.style.AppTheme)

		lifecycleScope.launchWhenCreated {
			if (viewModel.getSettings().showOnLockScreen) {
				Utils.toggleShowOnLockScreen(this@MainActivity, true)
			}
		}

		val binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
	}

	override fun dispatchKeyEvent(event: KeyEvent) =
		(supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.childFragmentManager?.fragments?.firstOrNull() as? FragmentBase<*>)
			?.onDispatchKeyEvent?.invoke(event) ?: super.dispatchKeyEvent(event)
}