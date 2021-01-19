package com.pointlessapps.tvremote_client.viewModels

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.tv.support.remote.core.Device
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.adapters.AdapterApplication
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.receivers.QtStateRefreshReceiver
import com.pointlessapps.tvremote_client.services.TvRemoteQTService
import com.pointlessapps.tvremote_client.tv.TvRemote
import com.pointlessapps.tvremote_client.utils.SwipeTouchHandler
import com.pointlessapps.tvremote_client.utils.runAsyncCatch
import kotlinx.android.synthetic.main.fragment_remote.view.*
import kotlin.system.exitProcess

class ViewModelRemote(
    private val activity: Activity,
    private val root: ViewGroup,
    private val deviceWrapper: DeviceWrapper
) : AndroidViewModel(activity.application) {

    private val context = activity.applicationContext
    private val remote = TvRemote(deviceWrapper)
    private val applications = listOf(
        R.mipmap.app_netflix to "com.netflix.ninja",
        R.mipmap.app_tidal to "com.aspiro.tidal",
        R.mipmap.app_youtube to "com.google.android.youtube.tv",
        R.mipmap.app_spotify to "com.spotify.tv.android",
        R.mipmap.app_cda to "pl.cda.tv",
        R.mipmap.app_play_store to "com.android.vending",
    )

    var onDispatchKeyEventListener: ((KeyEvent) -> Boolean)? = null

    @SuppressLint("ClickableViewAccessibility")
    fun setClickListeners() {
        root.buttonPower.setOnClickListener {
            runAsyncCatch({
                remote.powerOff(context)
                QtStateRefreshReceiver.sendBroadcast(
                    context,
                    TvRemoteQTService.ACTION_TOGGLE_POWER,
                    "off"
                )
                remote.disconnect()
                activity.finish()
            }) {
                remote.sendClick(KeyEvent.KEYCODE_POWER)
                remote.disconnect()
                activity.finish()
            }
        }
        root.buttonPower.setOnLongClickListener {
            remote.sendLongClick(KeyEvent.KEYCODE_POWER)
            true
        }
        root.buttonHome.setOnTouchListener { _, event ->
            remote.sendKeyEvent(KeyEvent.KEYCODE_HOME, event.action)
            true
        }
        root.buttonSource.setOnClickListener {
            remote.sendLongClick(KeyEvent.KEYCODE_TV_INPUT)
            remote.sendIntent(Intent("com.android.tv.action.VIEW_INPUTS").apply {
                setClassName("com.tcl.sourcemananger", "com.tcl.sourcemanager.MainActivity")
            })
        }
        root.buttonSource.setOnLongClickListener {
            remote.sendLongClick(KeyEvent.KEYCODE_LAST_CHANNEL)
            true
        }

        onDispatchKeyEventListener = { event ->
            event.takeIf {
                it.keyCode in arrayOf(
                    KeyEvent.KEYCODE_VOLUME_DOWN,
                    KeyEvent.KEYCODE_VOLUME_UP,
                    KeyEvent.KEYCODE_BACK
                )
            }?.let {
                remote.sendKeyEvent(it.keyCode, it.action)
                true
            } ?: false
        }
    }

    fun setTouchHandler() {
        root.containerTouchHandler.setOnTouchListener(SwipeTouchHandler {
            when (it) {
                SwipeTouchHandler.ACTION.LONG_CLICK -> remote.sendLongClick(KeyEvent.KEYCODE_DPAD_CENTER)
                SwipeTouchHandler.ACTION.CLICK -> remote.sendClick(KeyEvent.KEYCODE_DPAD_CENTER)
                SwipeTouchHandler.ACTION.SWIPE_LEFT -> remote.sendClick(KeyEvent.KEYCODE_DPAD_LEFT)
                SwipeTouchHandler.ACTION.SWIPE_RIGHT -> remote.sendClick(KeyEvent.KEYCODE_DPAD_RIGHT)
                SwipeTouchHandler.ACTION.SWIPE_UP -> remote.sendClick(KeyEvent.KEYCODE_DPAD_UP)
                SwipeTouchHandler.ACTION.SWIPE_DOWN -> remote.sendClick(KeyEvent.KEYCODE_DPAD_DOWN)
            }
        })
    }

    fun setApplicationsList() {
        root.listApplications.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = AdapterApplication(applications).apply {
                onClickListener = { remote.openApp(context, it.second) }
            }
        }
    }

    fun powerOn() {
        remote.powerOn(context)
        QtStateRefreshReceiver.sendBroadcast(
            context,
            TvRemoteQTService.ACTION_TOGGLE_POWER,
            "on"
        )
    }

    fun setOnDisconnectedListener() {
        deviceWrapper.setOnDisconnectedListener { reconnect() }
    }

    fun reconnect() {
        deviceWrapper.device = Device.from(
            context,
            deviceWrapper.device!!.deviceInfo,
            deviceWrapper.deviceListener,
            Handler(Looper.getMainLooper())
        )
    }

    fun isConnected() = deviceWrapper.device!!.isConnected
}