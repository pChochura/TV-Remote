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
import com.pointlessapps.tvremote_client.viewModels.ViewModelDeviceDiscovery

class FragmentDeviceDiscovery :
	FragmentBase<FragmentDeviceDiscoveryBinding>(FragmentDeviceDiscoveryBinding::inflate) {

	private val viewModel by viewModels<ViewModelDeviceDiscovery>()

	override fun created() {
		root.buttonRetry.setOnClickListener { viewModel.startDiscovery() }
		root.buttonSettings.setOnClickListener { findNavController().navigate(R.id.action_deviceDiscovery_to_settings) }

		refreshed()
	}

	override fun refreshed() {
		viewModel.state.observe(this) { state ->
			when (state) {
				ViewModelDeviceDiscovery.State.PAIRING -> {
					if (findNavController().currentDestination?.id == R.id.deviceDiscovery) {
						findNavController().navigate(R.id.action_deviceDiscovery_to_devicePairing)
					}
				}
				ViewModelDeviceDiscovery.State.CONNECTED -> {
					if (findNavController().currentDestination?.id == R.id.deviceDiscovery) {
						findNavController().navigate(R.id.action_deviceDiscovery_to_remote)
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