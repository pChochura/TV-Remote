package com.pointlessapps.tvremote_client

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.pointlessapps.tvremote_client.databinding.ActivityMainBinding
import com.pointlessapps.tvremote_client.fragments.FragmentDeviceDiscovery
import com.pointlessapps.tvremote_client.managers.FragmentManager
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.utils.loadShowOnLockScreen

class MainActivity : AppCompatActivity() {

    private val fragmentManager = FragmentManager.of(this, FragmentDeviceDiscovery())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.AppTheme)
        if (loadShowOnLockScreen()) {
            Utils.toggleShowOnLockScreen(this, true)
        }

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fragmentManager.showIn(R.id.fragmentContainer)
    }

    override fun dispatchKeyEvent(event: KeyEvent) =
        fragmentManager.dispatchKeyEvent(event) ?: super.dispatchKeyEvent(event)

    override fun onPause() {
        fragmentManager.onPauseActivity()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        fragmentManager.onResumeActivity()
    }
}