package com.pointlessapps.tvremote_client

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            val exec = Runtime.getRuntime().exec("input keyevent 120")
            exec.waitFor()
            val output: ByteArray = byteArrayOf()
            exec.outputStream.write(output)
            Log.d("LOG!", "output: ${output}, error: ${exec.errorStream}")
        } catch (e: Exception) {
            Log.d("LOG!", "error :c, $e")
        }
    }
}
