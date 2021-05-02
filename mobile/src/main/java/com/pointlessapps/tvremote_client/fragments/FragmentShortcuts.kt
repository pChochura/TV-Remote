package com.pointlessapps.tvremote_client.fragments

import android.app.Dialog
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.adapters.AdapterApplicationGrid
import com.pointlessapps.tvremote_client.databinding.FragmentShortcutsBinding
import com.pointlessapps.tvremote_client.models.Application
import com.pointlessapps.tvremote_client.viewModels.ViewModelSettings
import java.util.*

class FragmentShortcuts :
	FragmentBase<FragmentShortcutsBinding>(FragmentShortcutsBinding::inflate) {

	private val viewModel by activityViewModels<ViewModelSettings>()

	override fun created() {
		with(root.listApplications) {
			layoutManager = GridLayoutManager(requireActivity(), 2)
			adapter = AdapterApplicationGrid(viewModel.shortcuts).apply {
				onClickListener = { application ->
					showDialog(
						application,
						onSaveCallback = viewModel::updateShortcut,
						onDeleteCallback = viewModel::removeShortcut
					)
				}
			}
		}

		root.buttonAddShortcut.setOnClickListener {
			showDialog(onSaveCallback = viewModel::addShortcut)
		}
	}

	private fun showDialog(
		application: Application? = null,
		onSaveCallback: (Application) -> Unit,
		onDeleteCallback: ((Application) -> Unit)? = null
	) {
		Dialog(requireActivity()).apply {
			setTitle(R.string.add_custom_shortcut)
			setContentView(R.layout.dialog_add_shortcut)
			val icon = application?.icon ?: 0
			val editAppName = findViewById<EditText>(R.id.editAppName)
			val editPackageName = findViewById<EditText>(R.id.editPackageName)
			val imageApplication = findViewById<ImageView>(R.id.imageApplication)
			application?.also {
				editAppName.setText(application.activityName)
				editPackageName.setText(application.packageName)
				drawApplicationImage(it.icon, it.activityName, imageApplication)
			}
			editAppName.addTextChangedListener {
				drawApplicationImage(icon, it.toString(), imageApplication)
			}
			findViewById<View>(R.id.buttonSave).setOnClickListener {
				val appName = editAppName.text.toString()
				val packageName = editPackageName.text.toString()
				onSaveCallback(
					Application(
						icon,
						packageName,
						appName,
						id = application?.id ?: UUID.randomUUID().hashCode().toLong()
					)
				)
				dismiss()
			}
			findViewById<View>(R.id.buttonDelete).setOnClickListener {
				application?.also { onDeleteCallback?.invoke(it) }
				dismiss()
			}
		}.show()
	}

	private fun drawApplicationImage(
		icon: Int,
		text: String,
		imageApplication: ImageView
	) {
		if (icon == 0) {
			imageApplication.post {
				val width = imageApplication.width
				val height = imageApplication.height
				imageApplication.setImageBitmap(
					Application.getImageBitmap(
						text,
						root.root.context,
						width,
						height
					)
				)
			}
		} else {
			imageApplication.setImageResource(icon)
		}
	}
}