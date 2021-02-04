package com.pointlessapps.tvremote_client.viewModels

import android.os.Handler
import android.os.Looper
import android.transition.AutoTransition
import android.transition.TransitionManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.tv.support.remote.core.Device
import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.google.android.tv.support.remote.discovery.Discoverer
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.adapters.AdapterDevice
import com.pointlessapps.tvremote_client.databinding.FragmentDeviceDiscoveryBinding
import com.pointlessapps.tvremote_client.fragments.FragmentBase
import com.pointlessapps.tvremote_client.fragments.FragmentDevicePairing
import com.pointlessapps.tvremote_client.fragments.FragmentRemote
import com.pointlessapps.tvremote_client.fragments.FragmentSettings
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.utils.*

class ViewModelDeviceDiscovery(
    activity: AppCompatActivity,
    private val root: FragmentDeviceDiscoveryBinding
) :
    AndroidViewModel(activity.application) {

    private val context = activity.applicationContext
    private val discoverer = Discoverer(context)
    private val deviceWrapper = DeviceWrapper(null)
    private val devices = mutableListOf<DeviceInfo>()
    private val handler = Handler(Looper.getMainLooper())
    private val deviceListener = DeviceListenerImpl(deviceWrapper)
    private val discoveryListener = object : DiscoveryListenerImpl() {
        override fun onStartDiscoveryFailed(errorCode: Int) = setState(STATE.FAILED)
        override fun onDeviceFound(deviceInfo: DeviceInfo) {
            if (context.loadDeviceInfo() != null || devices.find { it.uri == deviceInfo.uri } != null) {
                return
            }

            devices.add(deviceInfo)
            root.listDevices.adapter?.notifyDataSetChanged()
            setState(STATE.FOUND)
        }

        override fun onDeviceLost(deviceInfo: DeviceInfo) {
            devices.removeIf { it.uri == deviceInfo.uri }
            root.listDevices.adapter?.notifyDataSetChanged()

            if (devices.isEmpty()) {
                setState(STATE.NO_DEVICES)
            }
        }
    }

    var onChangeFragmentListener: ((FragmentBase<*>) -> Unit)? = null
    var onPauseActivityListener: (() -> Unit)? = null
    var onResumeActivityListener: (() -> Unit)? = null

    init {
        Utils.getViewModel(ViewModelDevice::class.java, activity).deviceWrapper = deviceWrapper
    }

    fun setDeviceListener() {
        deviceWrapper.apply {
            setOnConnectFailedListener { setState(STATE.FAILED) }
            setOnConnectingListener { setState(STATE.LOADING) }
            setOnConnectedListener {
                onChangeFragmentListener?.invoke(FragmentRemote())
            }
            setOnDisconnectedListener { startDiscovery() }
            setOnPairingRequiredListener {
                setState(STATE.LOADING)
                onChangeFragmentListener?.invoke(FragmentDevicePairing())
            }
        }

        onPauseActivityListener = { discoverer.stopDiscovery() }
        onResumeActivityListener = { startDiscovery() }
    }

    fun loadDeviceInfo() {
        loadDevice(context.loadDeviceInfo() ?: return)
    }

    fun startDiscovery() {
        discoverer.startDiscovery(discoveryListener, handler)
        setState(STATE.SEARCHING)
    }

    fun setDeviceList() {
        root.listDevices.apply {
            adapter = AdapterDevice(devices).apply { onClickListener = { loadDevice(it) } }
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    fun setClickListeners() {
        root.buttonRetry.setOnClickListener {
            context.saveDeviceInfo(null)
            startDiscovery()
            setState(STATE.SEARCHING)
        }
        root.buttonSettings.setOnClickListener {
            onChangeFragmentListener?.invoke(FragmentSettings())
        }
    }

    private fun loadDevice(deviceInfo: DeviceInfo) {
        setState(STATE.LOADING)
        context.saveDeviceInfo(deviceInfo)
        deviceWrapper.device = Device.from(
            context,
            deviceInfo,
            deviceListener,
            handler
        )
    }

    private fun setState(state: STATE) {
        root.buttonRetry.isVisible = state.buttonRetryVisible
        root.imageError.isVisible = state.imageErrorVisible
        root.progress.isVisible = state.progressVisible
        root.listDevices.isVisible = state.listDevicesVisible
        root.textLabel.isVisible = state.textLabelVisible
        state.label?.let { root.textLabel.setText(it) }
        TransitionManager.beginDelayedTransition(root.root, AutoTransition())
    }

    private enum class STATE(
        val buttonRetryVisible: Boolean,
        val imageErrorVisible: Boolean,
        val progressVisible: Boolean,
        val listDevicesVisible: Boolean,
        val textLabelVisible: Boolean,
        @StringRes val label: Int? = null
    ) {
        LOADING(false, false, true, false, true, R.string.loading),
        SEARCHING(false, false, true, false, true, R.string.searching),
        FAILED(true, true, false, false, true, R.string.something_went_wrong),
        FOUND(false, false, false, true, false),
        NO_DEVICES(true, true, false, false, true, R.string.no_devices_found)
    }

}