package com.pointlessapps.tvremote_client.fragments

import android.transition.AutoTransition
import android.transition.TransitionManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.adapters.AdapterDevice
import com.pointlessapps.tvremote_client.databinding.FragmentDeviceDiscoveryBinding
import com.pointlessapps.tvremote_client.services.ConnectionService
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.utils.bindService
import com.pointlessapps.tvremote_client.viewModels.ViewModelDeviceDiscovery

class FragmentDeviceDiscovery :
	FragmentBase<FragmentDeviceDiscoveryBinding>(FragmentDeviceDiscoveryBinding::inflate) {

	private lateinit var service: ConnectionService
	private val viewModel by viewModels<ViewModelDeviceDiscovery>()

	override fun created() {
		requireActivity().bindService<ConnectionService.ConnectionBinder>(ConnectionService::class.java) {
			service = it?.service ?: return@bindService
			viewModel.setOnGetServiceCallback { service }
			viewModel.setDeviceListener()
			viewModel.loadDeviceInfo()
		}

		Utils.toggleShowOnLockScreen(requireActivity(), false)

		root.buttonRetry.setOnClickListener {
			viewModel.disconnect {
				viewModel.clearSavedDevice()
				viewModel.startDiscovery()
			}
		}
		root.buttonSettings.setOnClickListener {
			findNavController().navigate(R.id.actionDiscoveryToSettings)
		}

		refreshed()
	}

	override fun refreshed() {
		viewModel.state.observe(this) { state ->
			when (state) {
				ViewModelDeviceDiscovery.State.PAIRING -> {
					if (findNavController().currentDestination?.id == R.id.deviceDiscovery) {
						findNavController().navigate(R.id.actionDiscoveryToPairing)
					}
				}
				ViewModelDeviceDiscovery.State.CONNECTED -> {
					if (findNavController().currentDestination?.id == R.id.deviceDiscovery) {
						findNavController().navigate(R.id.actionDiscoveryToRemote)
					}
				}
				else -> {
					root.buttonRetry.isVisible = state.buttonRetryVisible
					root.imageError.isVisible = state.imageErrorVisible
					root.progress.isVisible = state.progressVisible
					root.listDevices.isVisible = state.listDevicesVisible
					root.textLabel.isVisible = state.textLabelVisible
					state.label?.let { root.textLabel.setText(it) }
					TransitionManager.beginDelayedTransition(root.root, AutoTransition())
				}
			}
		}

		viewModel.devices.observe(this) { devices ->
			root.listDevices.apply {
				adapter = AdapterDevice(devices).apply {
					onClickListener = { viewModel.loadDevice(it) }
				}
				layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
			}
		}
	}

	override fun onStart() {
		super.onStart()
		viewModel.startDiscovery()
	}

	override fun onPause() {
		viewModel.stopDiscovery()
		super.onPause()
	}

	override fun onResume() {
		super.onResume()
		viewModel.setDeviceListener()
		viewModel.loadDeviceInfo()
		viewModel.startDiscovery()
	}
}