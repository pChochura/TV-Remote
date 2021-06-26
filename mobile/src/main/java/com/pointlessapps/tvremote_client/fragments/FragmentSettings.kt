package com.pointlessapps.tvremote_client.fragments

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.adapters.AdapterApplicationToggleable
import com.pointlessapps.tvremote_client.databinding.FragmentSettingsBinding
import com.pointlessapps.tvremote_client.utils.DragItemTouchHelper
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelSettings

class FragmentSettings : FragmentBase<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

	private val viewModel by activityViewModels<ViewModelSettings>()

	override fun created() {
		prepareSettings()
		prepareClickListeners()
	}

	private fun prepareSettings() {
		viewModel.settings.observe(this) { settings ->
			root.toggleTurnOnTv.isChecked = settings.turnOnTv
			root.toggleCloseApplication.isChecked = settings.closeApplication
			root.toggleShowDpad.isChecked = settings.showDpad
			root.toggleShowOnLockScreen.isChecked = settings.showOnLockScreen
			root.toggleOpenLastConnection.isChecked = settings.openLastConnection
			root.toggleVibrationEnabled.isChecked = settings.vibrationEnabled
		}

		with(root.listShortcuts) {
			layoutManager =
				LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
			adapter = AdapterApplicationToggleable(viewModel.shortcuts).apply {
				onChangeToggleListener = { viewModel.setShortcuts(it) }
			}
			ItemTouchHelper(DragItemTouchHelper(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) { _, from, to ->
				viewModel.setShortcuts(
					viewModel.shortcuts.value?.toMutableList()?.also {
						val temp = it[from]
						it[from] = it[to]
						it[to] = temp
					} ?: return@DragItemTouchHelper
				)
			}).attachToRecyclerView(this)
		}
	}

	private fun prepareClickListeners() {
		root.containerTurnOnTv.setOnClickListener {
			viewModel.setTurnOnTv(!root.toggleTurnOnTv.isChecked)
		}
		root.containerCloseApplication.setOnClickListener {
			viewModel.setCloseApplication(!root.toggleCloseApplication.isChecked)
		}
		root.containerShowDpad.setOnClickListener {
			viewModel.setShowDpad(!root.toggleShowDpad.isChecked)
		}
		root.containerShowOnLockScreen.setOnClickListener {
			viewModel.setShowOnLockScreen(!root.toggleShowOnLockScreen.isChecked)
		}
		root.containerOpenLastConnection.setOnClickListener {
			viewModel.setOpenLastConnection(!root.toggleOpenLastConnection.isChecked)
		}
		root.containerVibrationEnabled.setOnClickListener {
			viewModel.setVibrationEnabled(!root.toggleVibrationEnabled.isChecked)
		}

		root.toggleTurnOnTv.setOnCheckedChangeListener { _, isChecked ->
			viewModel.setTurnOnTv(isChecked)
		}
		root.toggleCloseApplication.setOnCheckedChangeListener { _, isChecked ->
			viewModel.setCloseApplication(isChecked)
		}
		root.toggleShowDpad.setOnCheckedChangeListener { _, isChecked ->
			viewModel.setShowDpad(isChecked)
		}
		root.toggleShowOnLockScreen.setOnCheckedChangeListener { _, isChecked ->
			viewModel.setShowOnLockScreen(isChecked)
		}
		root.toggleOpenLastConnection.setOnCheckedChangeListener { _, isChecked ->
			viewModel.setOpenLastConnection(isChecked)
		}
		root.toggleVibrationEnabled.setOnCheckedChangeListener { _, isChecked ->
			viewModel.setVibrationEnabled(isChecked)
		}
		root.containerShortcuts.setOnClickListener {
			findNavController().navigate(R.id.actionSettingsToShortcuts)
		}
	}
}