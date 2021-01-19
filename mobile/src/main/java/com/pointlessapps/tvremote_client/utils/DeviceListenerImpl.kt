package com.pointlessapps.tvremote_client.utils

import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import com.google.android.tv.support.remote.core.Device
import com.pointlessapps.tvremote_client.models.DeviceWrapper

class DeviceListenerImpl(private val deviceWrapper: DeviceWrapper) : Device.Listener() {

    init { deviceWrapper.deviceListener = this }

    override fun onConnected(device: Device) { deviceWrapper.onConnected?.invoke(device) }
    override fun onPairingRequired(device: Device) { deviceWrapper.onPairingRequired?.invoke(device) }
    override fun onConnecting(device: Device) { deviceWrapper.onConnecting?.invoke(device) }
    override fun onConnectFailed(device: Device) { deviceWrapper.onConnectFailed?.invoke(device) }
    override fun onDisconnected(device: Device) { deviceWrapper.onDisconnected?.invoke(device) }
    override fun onHideIme(device: Device) { deviceWrapper.onHideIme?.invoke(device) }
    override fun onStartVoice(device: Device) { deviceWrapper.onStartVoice?.invoke(device) }
    override fun onVoiceSoundLevel(device: Device, voiceSoundLevel: Int) = Unit
    override fun onStopVoice(device: Device) { deviceWrapper.onStopVoice?.invoke(device) }
    override fun onConfigureSuccess(device: Device) = Unit
    override fun onConfigureFailure(device: Device, p1: Int) = Unit
    override fun onException(device: Device, exception: Exception) = Unit
    override fun onDeveloperStatus(device: Device, p1: Boolean) = Unit
    override fun onBugReportStatus(device: Device, p1: Int) = Unit
    override fun onCompletionInfo(device: Device, infos: Array<out CompletionInfo>) { deviceWrapper.onCompletionInfo?.invoke(device) }
    override fun onShowIme(device: Device, editorInfo: EditorInfo, p2: Boolean, extractedText: ExtractedText) { deviceWrapper.onShowIme?.invoke(device, editorInfo) }
    override fun onAsset(device: Device, p1: String?, p2: MutableMap<String, String>?, p3: ByteArray?) = Unit
}