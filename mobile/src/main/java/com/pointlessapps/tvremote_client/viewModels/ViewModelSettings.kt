package com.pointlessapps.tvremote_client.viewModels

import android.annotation.SuppressLint
import android.app.Dialog
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.adapters.AdapterApplicationToggleable
import com.pointlessapps.tvremote_client.databinding.FragmentSettingsBinding
import com.pointlessapps.tvremote_client.models.Application
import com.pointlessapps.tvremote_client.utils.*

@SuppressLint("StaticFieldLeak")
class ViewModelSettings(
    private val activity: AppCompatActivity,
    private val root: FragmentSettingsBinding
) : AndroidViewModel(activity.application) {

    fun prepareSettings() {
        root.toggleTurnOnTv.isChecked = activity.loadTurnTvOn()
        root.toggleCloseApplication.isChecked = activity.loadCloseApplication()
        root.toggleShowDpad.isChecked = activity.loadShowDpad()
        root.toggleShowOnLockScreen.isChecked = activity.loadShowOnLockScreen()
        root.toggleOpenLastConnection.isChecked = activity.loadOpenLastConnection()

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
        root.containerShowOnLockScreen.setOnClickListener {
            root.toggleShowOnLockScreen.isChecked = !root.toggleShowOnLockScreen.isChecked
            activity.saveShowOnLockScreen(root.toggleShowOnLockScreen.isChecked)
            Utils.toggleShowOnLockScreen(activity, root.toggleShowOnLockScreen.isChecked)
        }
        root.containerOpenLastConnection.setOnClickListener {
            root.toggleOpenLastConnection.isChecked = !root.toggleOpenLastConnection.isChecked
            activity.saveOpenLastConnection(root.toggleOpenLastConnection.isChecked)
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
        root.toggleShowOnLockScreen.setOnCheckedChangeListener { _, isChecked ->
            activity.saveShowOnLockScreen(isChecked)
            Utils.toggleShowOnLockScreen(activity, isChecked)
        }
        root.toggleOpenLastConnection.setOnCheckedChangeListener { _, isChecked ->
            activity.saveOpenLastConnection(isChecked)
        }
        root.buttonAddShortcut.setOnClickListener {
            Dialog(activity).apply {
                setTitle(R.string.add_custom_shortcut)
                setContentView(R.layout.dialog_add_shortcut)
                findViewById<View>(R.id.buttonAdd).setOnClickListener {
                    val appName = findViewById<EditText>(R.id.editAppName).text.toString()
                    val packageName = findViewById<EditText>(R.id.editPackageName).text.toString()
                    val shortcuts = activity.loadShortcuts().toMutableList().apply {
                        add(Application(0, packageName, appName))
                    }
                    activity.saveShortcuts(shortcuts)
                    (root.listShortcuts.adapter as? AdapterApplicationToggleable)?.updateList(
                        shortcuts
                    )
                    dismiss()
                }
            }.show()
        }
    }
}