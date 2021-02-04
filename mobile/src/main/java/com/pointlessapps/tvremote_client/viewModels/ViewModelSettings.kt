package com.pointlessapps.tvremote_client.viewModels

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapps.tvremote_client.adapters.AdapterApplicationToggleable
import com.pointlessapps.tvremote_client.databinding.FragmentSettingsBinding
import com.pointlessapps.tvremote_client.utils.*

class ViewModelSettings(
    private val activity: AppCompatActivity,
    private val root: FragmentSettingsBinding
) : AndroidViewModel(activity.application) {

    fun prepareSettings() {
        root.toggleTurnOnTv.isChecked = activity.loadTurnTvOn()
        root.toggleCloseApplication.isChecked = activity.loadCloseApplication()
        root.toggleShowDpad.isChecked = activity.loadShowDpad()

        root.listShortcuts.apply {
            layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
            adapter = AdapterApplicationToggleable(activity.loadShortcuts()).apply {
                onChangeToggleListener = { activity.saveShortcuts(it) }
            }
            ItemTouchHelper(DragItemTouchHelper(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) { adapter, from, to ->
                (adapter as? AdapterApplicationToggleable)?.also {
                    it.swapItems(from, to)
                    activity.saveShortcuts(it.list())
                }
            }).attachToRecyclerView(this)
        }
    }

    fun prepareClickListeners() {
        root.containerTurnOnTv.setOnClickListener {
            root.toggleTurnOnTv.isChecked = !root.toggleTurnOnTv.isChecked
            activity.saveTurnTvOn(root.toggleTurnOnTv.isChecked)
        }
        root.containerCloseApplication.setOnClickListener {
            root.toggleCloseApplication.isChecked = !root.toggleCloseApplication.isChecked
            activity.saveCloseApplication(root.toggleCloseApplication.isChecked)
        }
        root.containerShowDpad.setOnClickListener {
            root.toggleShowDpad.isChecked = !root.toggleShowDpad.isChecked
            activity.saveShowDpad(root.toggleShowDpad.isChecked)
        }

        root.toggleTurnOnTv.setOnCheckedChangeListener { _, isChecked ->
            activity.saveTurnTvOn(isChecked)
        }
        root.toggleCloseApplication.setOnCheckedChangeListener { _, isChecked ->
            activity.saveTurnTvOn(isChecked)
        }
        root.toggleShowDpad.setOnCheckedChangeListener { _, isChecked ->
            activity.saveShowDpad(isChecked)
        }
    }
}