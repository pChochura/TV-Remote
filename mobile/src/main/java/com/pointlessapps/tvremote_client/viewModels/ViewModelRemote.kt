package com.pointlessapps.tvremote_client.viewModels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.TileService
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.tv.support.remote.core.Device
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.adapters.AdapterApplication
import com.pointlessapps.tvremote_client.models.Application
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.services.TvRemoteQTService
import com.pointlessapps.tvremote_client.tv.TvRemote
import com.pointlessapps.tvremote_client.utils.*
import kotlinx.android.synthetic.main.fragment_remote.view.*

class ViewModelRemote(
    private val activity: AppCompatActivity,
    private val root: ViewGroup,
    private val deviceWrapper: DeviceWrapper
) : AndroidViewModel(activity.application) {

    companion object {
        const val REQUEST_CODE_AUDIO = 1
    }

    private val context = activity.applicationContext
    private val remote = TvRemote(deviceWrapper)

    var onDispatchKeyEventListener: ((KeyEvent) -> Boolean)? = null
    var onPauseActivityListener: (() -> Unit)? = null
    var onResumeActivityListener: (() -> Unit)? = null
    var onPopBackStackListener: (() -> Unit)? = null

    @SuppressLint("ClickableViewAccessibility")
    fun setClickListeners() {
        root.buttonPower.setOnClickListener {
            runAsyncCatch({
                remote.powerOff(context)
                TileService.requestListeningState(
                    activity,
                    ComponentName(activity, TvRemoteQTService::class.java)
                )
                if (context.loadCloseApplication()) {
                    remote.disconnect()
                    activity.finish()
                }
            }) {
                remote.sendClick(KeyEvent.KEYCODE_POWER)
                if (context.loadCloseApplication()) {
                    remote.disconnect()
                    activity.finish()
                }
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
            remote.sendClick(KeyEvent.KEYCODE_TV_INPUT)
            remote.sendIntent(Intent("com.android.tv.action.VIEW_INPUTS").apply {
                setClassName("com.tcl.sourcemananger", "com.tcl.sourcemanager.MainActivity")
            })
        }
        root.buttonSource.setOnLongClickListener {
            remote.sendClick(KeyEvent.KEYCODE_SETTINGS)
            true
        }
        root.buttonClose.setOnClickListener {
            remote.disconnect()
            context.saveDeviceInfo(null)
            onPopBackStackListener?.invoke()
        }

        onDispatchKeyEventListener = { event ->
            event.takeIf {
                it.keyCode in arrayOf(
                    KeyEvent.KEYCODE_VOLUME_DOWN,
                    KeyEvent.KEYCODE_VOLUME_UP,
                    KeyEvent.KEYCODE_BACK
                )
            }?.let {
                if (deviceWrapper.device?.isVoiceRecording == true) {
                    deviceWrapper.device?.stopVoice()
                } else {
                    remote.sendKeyEvent(it.keyCode, it.action)
                }
                true
            } ?: false
        }
    }

    fun setStateListeners() {
        onPauseActivityListener = { deviceWrapper.device?.disconnect() }
        onResumeActivityListener = {
            reconnect()
            root.progress.isVisible = true
        }

        deviceWrapper.setOnDisconnectedListener {
            reconnect()
            root.progress.isVisible = true
        }
        deviceWrapper.setOnConnectedListener {
            root.progress.isVisible = false
            deviceWrapper.device?.beginBatchEdit()
        }

        setKeyboardInputListener()
        setVoiceInputListener()
    }

    private fun setKeyboardInputListener() {
        val textWatcher = root.editInput.addTextChangedListener {
            deviceWrapper.device?.setComposingText(it?.toString(), root.editInput.selectionStart)
        }
        root.editInput.removeTextChangedListener(textWatcher)

        deviceWrapper.setOnShowImeListener { _, info, text ->
            info.hintText.takeIf { it.isNotEmpty() }?.let { root.editInput.hint = it }
            root.editInput.imeOptions = info.imeOptions
            if (root.containerKeyboardInput.isGone) {
                root.editInput.removeTextChangedListener(textWatcher)
                root.editInput.setText(text?.text)
                root.editInput.addTextChangedListener(textWatcher)
                deviceWrapper.device?.setComposingRegion(0, text?.text?.length ?: 0)
                root.containerKeyboardInput.isVisible = true
                Utils.showKeyboard(context, root.editInput)
                if (text?.selectionStart ?: -1 != -1 && text?.selectionEnd ?: -1 != -1) {
                    root.editInput.setSelection(text?.selectionStart!!, text.selectionEnd)
                }
            }
        }
        deviceWrapper.setOnHideImeListener {
            if (root.containerKeyboardInput.isVisible) {
                root.containerKeyboardInput.isVisible = false
                root.editInput.removeTextChangedListener(textWatcher)
                Utils.hideKeyboard(context, root.editInput)
            }
        }
        Utils.setOnKeyboardChangeVisibilityListener(root) {
            it.takeIf { !it }?.let { deviceWrapper.device?.endBatchEdit() }
        }
        root.editInput.setOnEditorActionListener { _, actionId, _ ->
            deviceWrapper.device?.performEditorAction(actionId)
            true
        }
    }

    private fun setVoiceInputListener() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                Dialog(activity).apply {
                    setTitle(R.string.voice_search_permission)
                    setContentView(R.layout.dialog_message)
                    setOnDismissListener {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            REQUEST_CODE_AUDIO
                        )
                    }
                }.show()
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_CODE_AUDIO
                )
            }
        }

        root.imageMicrophone.scaleAnimation()
        deviceWrapper.setOnStartVoiceListener {
            root.imageMicrophone.scaleAnimation()
            root.containerVoiceInput.isVisible = true
        }
        deviceWrapper.setOnStopVoiceListener {
            root.containerVoiceInput.isVisible = false
            root.imageMicrophone.clearAnimation()
        }
        root.buttonCancelVoiceInput.setOnClickListener {
            deviceWrapper.device?.stopVoice()
        }
    }

    fun onPermissionResult(requestCode: Int, grantResults: IntArray) =
        (requestCode == REQUEST_CODE_AUDIO && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED).takeIf { it }
            ?.also {
                Toast.makeText(context, R.string.now_you_can_use_voice_search, Toast.LENGTH_SHORT)
                    .show()
            } ?: false

    fun setTouchHandler() {
        root.imageGestureNavigation.isVisible = true
        root.containerDpad.isVisible = false
        root.containerTouchHandler.setOnTouchListener(SwipeTouchHandler {
            when (it) {
                SwipeTouchHandler.ACTION.LONG_CLICK -> remote.sendLongClick(KeyEvent.KEYCODE_DPAD_CENTER)
                SwipeTouchHandler.ACTION.CLICK -> {
                    remote.sendClick(KeyEvent.KEYCODE_DPAD_CENTER)
                    deviceWrapper.device?.beginBatchEdit()
                }
                SwipeTouchHandler.ACTION.SWIPE_LEFT -> remote.sendClick(KeyEvent.KEYCODE_DPAD_LEFT)
                SwipeTouchHandler.ACTION.SWIPE_RIGHT -> remote.sendClick(KeyEvent.KEYCODE_DPAD_RIGHT)
                SwipeTouchHandler.ACTION.SWIPE_UP -> remote.sendClick(KeyEvent.KEYCODE_DPAD_UP)
                SwipeTouchHandler.ACTION.SWIPE_DOWN -> remote.sendClick(KeyEvent.KEYCODE_DPAD_DOWN)
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setDpad() {
        root.imageGestureNavigation.isVisible = false
        root.containerDpad.isVisible = true
        root.buttonLeft.setOnTouchListener { _, event ->
            true.also { remote.sendKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, event.action) }
        }
        root.buttonRight.setOnTouchListener { _, event ->
            true.also { remote.sendKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT, event.action) }
        }
        root.buttonUp.setOnTouchListener { _, event ->
            true.also { remote.sendKeyEvent(KeyEvent.KEYCODE_DPAD_UP, event.action) }
        }
        root.buttonDown.setOnTouchListener { _, event ->
            true.also { remote.sendKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, event.action) }
        }
        root.buttonOK.setOnTouchListener { _, event ->
            true.also {
                remote.sendKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER, event.action)
                deviceWrapper.device?.beginBatchEdit()
            }
        }
    }

    fun setApplicationsList() {
        root.listApplications.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter =
                AdapterApplication(activity.loadShortcuts().filter(Application::checked)).apply {
                    onClickListener = { remote.openApp(context, it.packageName, it.activityName) }
                }
        }
    }

    fun powerOn() {
        remote.powerOn(context)
        TileService.requestListeningState(
            activity,
            ComponentName(activity, TvRemoteQTService::class.java)
        )
    }

    private fun reconnect() {
        deviceWrapper.device = Device.from(
            context,
            deviceWrapper.device!!.deviceInfo,
            deviceWrapper.deviceListener,
            Handler(Looper.getMainLooper())
        )
    }
}