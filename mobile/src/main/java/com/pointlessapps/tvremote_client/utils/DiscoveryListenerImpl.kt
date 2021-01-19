package com.pointlessapps.tvremote_client.utils

import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.google.android.tv.support.remote.discovery.Discoverer

open class DiscoveryListenerImpl : Discoverer.DiscoveryListener() {
    override fun onDiscoveryStarted() = Unit
    override fun onDeviceFound(deviceInfo: DeviceInfo) = Unit
    override fun onDeviceLost(deviceInfo: DeviceInfo) = Unit
    override fun onStartDiscoveryFailed(errorCode: Int) = Unit
}