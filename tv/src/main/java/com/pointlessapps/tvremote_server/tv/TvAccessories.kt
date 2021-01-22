package com.pointlessapps.tvremote_server.tv

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import com.pointlessapps.tvremote_server.utils.isDeviceConnected
import com.pointlessapps.tvremote_server.utils.setDeviceConnected
import org.json.JSONObject

object TvAccessories {

    fun getBtAccessories(context: Context) =
        BluetoothAdapter.getDefaultAdapter().bondedDevices.map {
            JSONObject(
                """
                {
                    "name": "${it.name}",
                    "address": "${it.address}",
                    "connected": "${context.isDeviceConnected(it.address)}"
                }
                """.trimIndent()
            )
        }

    fun startBtDiscovery(context: Context) {
        context.startActivity(Intent(Intent.ACTION_MAIN).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            setClassName(
                "com.android.tv.settings",
                "com.android.tv.settings.accessories.AddAccessoryActivity"
            )
        })
    }

    fun connectBtDevice(context: Context, address: String) {
        BluetoothAdapter.getDefaultAdapter().apply {
            val device = getRemoteDevice(address)
            getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                override fun onServiceDisconnected(profile: Int) = Unit
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    if (
                        BluetoothA2dp::class.java.getMethod(
                            "connect",
                            BluetoothDevice::class.java
                        ).invoke(proxy, device) == true
                    ) {
                        context.setDeviceConnected(device.address)
                    }
                }
            }, BluetoothProfile.A2DP)
        }
    }

    fun disconnectBtDevice(context: Context, address: String) {
        BluetoothAdapter.getDefaultAdapter().apply {
            val device = getRemoteDevice(address)
            getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                override fun onServiceDisconnected(profile: Int) = Unit
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    if (
                        BluetoothA2dp::class.java.getMethod(
                            "disconnect",
                            BluetoothDevice::class.java
                        ).invoke(proxy, device) == true
                    ) {
                        context.setDeviceConnected(device.address, false)
                    }
                }
            }, BluetoothProfile.A2DP)
        }
    }
}