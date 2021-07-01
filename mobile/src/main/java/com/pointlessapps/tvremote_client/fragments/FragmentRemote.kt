package com.pointlessapps.tvremote_client.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.view.KeyEvent
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.adapters.AdapterApplicationList
import com.pointlessapps.tvremote_client.databinding.FragmentRemoteBinding
import com.pointlessapps.tvremote_client.services.ConnectionService
import com.pointlessapps.tvremote_client.utils.SwipeTouchHandler
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.utils.bindService
import com.pointlessapps.tvremote_client.utils.scaleAnimation
import com.pointlessapps.tvremote_client.viewModels.ViewModelRemote

class FragmentRemote : FragmentBase<FragmentRemoteBinding>(FragmentRemoteBinding::inflate) {

	private lateinit var service: ConnectionService
	private val viewModel by activityViewModels<ViewModelRemote>()

	private val permissionLauncher =
		registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

	override fun created() {
		requireActivity().bindService<ConnectionService.ConnectionBinder>(ConnectionService::class.java) {
			service = it?.service ?: return@bindService
			viewModel.setOnGetServiceCallback { service }
			viewModel.setDeviceListener()
			viewModel.powerOnIfNecessary()

			setKeyboardInputListener()
			setVoiceInputListener()
			setClickListeners()
			setApplicationsList()
			viewModel.checkConnectionState()
		}

		viewModel.toggleShowOnLockScreenIfEnabled(requireActivity())

		lifecycleScope.launchWhenStarted {
			val settings = viewModel.getSettings()

			when {
				settings.showDpad -> setDpad()
				else -> setTouchHandler()
			}
		}

		viewModel.isLoading.observe(this) {
			root.progress.isVisible = it
		}

		requireActivity().onBackPressedDispatcher.addCallback(this) {
			viewModel.sendClick(KeyEvent.KEYCODE_BACK)
		}
	}

	override fun refreshed() {
		viewModel.checkConnectionState()
	}

	private fun setVoiceInputListener() {
		if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO)
			!= PackageManager.PERMISSION_GRANTED
		) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(
					requireActivity(),
					Manifest.permission.RECORD_AUDIO
				)
			) {
				Dialog(requireActivity()).apply {
					setTitle(R.string.voice_search_permission)
					setContentView(R.layout.dialog_message)
					setOnDismissListener {
						permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
					}
				}.show()
			} else {
				permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
			}
		}

		viewModel.isVoiceRecording.observe(this) {
			root.containerVoiceInput.isVisible = it
			if (it) {
				root.imageMicrophone.scaleAnimation()
			} else {
				root.imageMicrophone.clearAnimation()
			}
		}
		root.buttonCancelVoiceInput.setOnClickListener { viewModel.stopVoiceRecording() }
	}

	@SuppressLint("ClickableViewAccessibility")
	private fun setClickListeners() {
		root.buttonPower.setOnClickListener {
			viewModel.vibrateIfEnabled()
			viewModel.powerOff { requireActivity().finish() }
			requireActivity().startService(
				Intent(context, ConnectionService::class.java).putExtra(
					ConnectionService.DISCONNECT,
					true
				)
			)
		}
		root.buttonPower.setOnLongClickListener {
			viewModel.vibrateIfEnabled()
			viewModel.sendLongClick(KeyEvent.KEYCODE_POWER)
			true
		}
		root.buttonHome.setOnTouchListener { _, event ->
			viewModel.vibrateIfEnabled()
			viewModel.sendKeyEvent(KeyEvent.KEYCODE_HOME, event.action)
			true
		}
		root.buttonSource.setOnClickListener {
			viewModel.vibrateIfEnabled()
			viewModel.sendClick(KeyEvent.KEYCODE_TV_INPUT)
			viewModel.sendIntent(Intent("com.android.tv.action.VIEW_INPUTS").apply {
				setClassName("com.tcl.sourcemananger", "com.tcl.sourcemanager.MainActivity")
			})
		}
		root.buttonSource.setOnLongClickListener {
			viewModel.vibrateIfEnabled()
			viewModel.sendClick(KeyEvent.KEYCODE_SETTINGS)
			true
		}
		root.buttonClose.setOnClickListener {
			viewModel.vibrateIfEnabled()
			viewModel.setDeviceInfo(null)
			viewModel.disconnect {
				findNavController().navigate(R.id.actionRemoteToDiscovery)
			}
		}

		onDispatchKeyEvent = { event ->
			event.takeIf {
				it.keyCode in arrayOf(
					KeyEvent.KEYCODE_VOLUME_DOWN,
					KeyEvent.KEYCODE_VOLUME_UP,
					KeyEvent.KEYCODE_BACK
				)
			}?.let {
				viewModel.vibrateIfEnabled()
				viewModel.sendKeyEvent(it.keyCode, it.action)
				true
			} ?: false
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	private fun setTouchHandler() {
		root.imageGestureNavigation.isVisible = true
		root.containerDpad.isVisible = false
		root.containerTouchHandler.setOnTouchListener(SwipeTouchHandler {
			when (it) {
				SwipeTouchHandler.ACTION.LONG_CLICK -> {
					viewModel.vibrateIfEnabled()
					viewModel.sendLongClick(KeyEvent.KEYCODE_DPAD_CENTER)
				}
				SwipeTouchHandler.ACTION.CLICK -> {
					viewModel.vibrateIfEnabled()
					viewModel.sendClick(KeyEvent.KEYCODE_DPAD_CENTER)
					viewModel.beginBatchEdit()
				}
				SwipeTouchHandler.ACTION.SWIPE_LEFT -> {
					viewModel.vibrateIfEnabled()
					viewModel.sendClick(KeyEvent.KEYCODE_DPAD_LEFT)
				}
				SwipeTouchHandler.ACTION.SWIPE_RIGHT -> {
					viewModel.vibrateIfEnabled()
					viewModel.sendClick(KeyEvent.KEYCODE_DPAD_RIGHT)
				}
				SwipeTouchHandler.ACTION.SWIPE_UP -> {
					viewModel.vibrateIfEnabled()
					viewModel.sendClick(KeyEvent.KEYCODE_DPAD_UP)
				}
				SwipeTouchHandler.ACTION.SWIPE_DOWN -> {
					viewModel.vibrateIfEnabled()
					viewModel.sendClick(KeyEvent.KEYCODE_DPAD_DOWN)
				}
			}
		})
	}

	private fun setKeyboardInputListener() {
		val textWatcher = root.editInput.addTextChangedListener {
			viewModel.setComposingText(
				it?.toString() ?: return@addTextChangedListener,
				root.editInput.selectionStart
			)
		}
		root.editInput.removeTextChangedListener(textWatcher)

		viewModel.setOnShowImeListener { editorInfo, text ->
			editorInfo?.also { info ->
				info.hintText.takeIf { it.isNotEmpty() }?.let { root.editInput.hint = it }
				root.editInput.imeOptions = info.imeOptions
			}
			if (root.containerKeyboardInput.isGone) {
				root.editInput.removeTextChangedListener(textWatcher)
				root.editInput.setText(text?.text)
				root.editInput.addTextChangedListener(textWatcher)
				viewModel.setComposingRegion(0, text?.text?.length ?: 0)
				root.containerKeyboardInput.isVisible = true
				Utils.showKeyboard(requireContext(), root.editInput)
				if (text?.selectionStart ?: -1 != -1 && text?.selectionEnd ?: -1 != -1) {
					root.editInput.setSelection(text?.selectionStart!!, text.selectionEnd)
				}
			}
		}

		viewModel.setOnHideImeListener {
			if (root.containerKeyboardInput.isVisible) {
				root.containerKeyboardInput.isVisible = false
				root.editInput.removeTextChangedListener(textWatcher)
				Utils.hideKeyboard(requireContext(), root.editInput)
			}
		}
		Utils.setOnKeyboardChangeVisibilityListener(root.root) {
			it.takeIf { !it }?.let { viewModel.endBatchEdit() }
		}
		root.editInput.setOnEditorActionListener { _, actionId, _ ->
			viewModel.performEditAction(actionId)
			true
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	private fun setDpad() {
		root.imageGestureNavigation.isVisible = false
		root.containerDpad.isVisible = true
		root.buttonLeft.setOnTouchListener { _, event ->
			viewModel.vibrateIfEnabled()
			viewModel.sendKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, event.action)
			true
		}
		root.buttonRight.setOnTouchListener { _, event ->
			viewModel.vibrateIfEnabled()
			viewModel.sendKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT, event.action)
			true
		}
		root.buttonUp.setOnTouchListener { _, event ->
			viewModel.vibrateIfEnabled()
			viewModel.sendKeyEvent(KeyEvent.KEYCODE_DPAD_UP, event.action)
			true
		}
		root.buttonDown.setOnTouchListener { _, event ->
			viewModel.vibrateIfEnabled()
			viewModel.sendKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, event.action)
			true
		}
		root.buttonOK.setOnTouchListener { _, event ->
			viewModel.vibrateIfEnabled()
			viewModel.sendKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER, event.action)
			viewModel.beginBatchEdit()
			true
		}
	}

	private fun setApplicationsList() {
		viewModel.checkedShortcuts.observe(this) { shortcuts ->
			root.listApplications.apply {
				layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
				adapter = AdapterApplicationList(shortcuts).apply {
					onClickListener = {
						viewModel.vibrateIfEnabled()
						viewModel.openApp(it.packageName, it.activityName)
					}
				}
			}
		}
	}
}