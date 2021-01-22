package com.pointlessapps.tvremote_client

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.pointlessapps.tvremote_client.fragments.FragmentDeviceDiscovery
import com.pointlessapps.tvremote_client.managers.FragmentManager
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelDevice

class MainActivity : AppCompatActivity() {

    private val fragmentManager = FragmentManager.of(this, FragmentDeviceDiscovery())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) setShowWhenLocked(
            true
        ) else window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        setContentView(R.layout.activity_main)

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