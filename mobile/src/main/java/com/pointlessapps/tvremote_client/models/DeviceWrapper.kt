package com.pointlessapps.tvremote_client.models

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import com.google.android.tv.support.remote.core.Device

class DeviceWrapper(var device: Device?) {

    internal var deviceListener: Device.Listener? = null

    internal var onConnected: ((Device) -> Unit)? = null
    internal var onPairingRequired: ((Device) -> Unit)? = null
    internal var onCompletionInfo: ((Device) -> Unit)? = null
    internal var onShowIme: ((Device, EditorInfo, ExtractedText?) -> Unit)? = null
    internal var onConnecting: ((Device) -> Unit)? = null
    internal var onConnectFailed: ((Device) -> Unit)? = null
    internal var onDisconnected: ((Device) -> Unit)? = null
    internal var onHideIme: ((Device) -> Unit)? = null
    internal var onStartVoice: ((Device) -> Unit)? = null
    internal var onVoiceSoundLevel: ((Device, Int) -> Unit)? = null
    internal var onStopVoice: ((Device) -> Unit)? = null

    fun setOnConnectedListener(listener: (Device) -> Unit) { onConnected = listener }
    fun setOnPairingRequiredListener(listener: (Device) -> Unit) { onPairingRequired = listener }
    fun setOnCompletionInfoListener(listener: (Device) -> Unit) { onCompletionInfo = listener }
    fun setOnShowImeListener(listener: (Device, EditorInfo, ExtractedText?) -> Unit) { onShowIme = listener }
    fun setOnConnectingListener(listener: (Device) -> Unit) { onConnecting = listener }
    fun setOnConnectFailedListener(listener: (Device) -> Unit) { onConnectFailed = listener }
    fun setOnDisconnectedListener(listener: (Device) -> Unit) { onDisconnected = listener }
    fun setOnHideImeListener(listener: (Device) -> Unit) { onHideIme = listener }
    fun setOnStartVoiceListener(listener: (Device) -> Unit) { onStartVoice = listener }
    fun setOnVoiceSoundLevelListener(listener: (Device, level: Int) -> Unit) { onVoiceSoundLevel = listener }
    fun setOnStopVoiceListener(listener: (Device) -> Unit) { onStopVoice = listener }
}
