package com.pointlessapps.tvremote_server

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createServer()
    }

    private fun createServer() {
        startService(Intent(this, BackgroundService::class.java))
    }
}
