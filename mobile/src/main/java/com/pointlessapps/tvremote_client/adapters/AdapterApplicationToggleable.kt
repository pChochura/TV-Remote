package com.pointlessapps.tvremote_client.adapters

import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import com.pointlessapps.tvremote_client.databinding.ItemApplicationToggleableBinding
import com.pointlessapps.tvremote_client.models.Application

class AdapterApplicationToggleable(apps: LiveData<List<Application>>) :
	AdapterBase<Application, ItemApplicationToggleableBinding>(
		apps,
		ItemApplicationToggleableBinding::inflate
	) {

	init {
		setHasStableIds(true)
	}

	var onChangeToggleListener: ((List<Application>) -> Unit)? = null

	override fun getItemId(position: Int) = list.value?.getOrNull(position)?.id ?: 0

	override fun onBind(root: ItemApplicationToggleableBinding, item: Application) {
		if (item.icon == 0) {
			root.imageApplication.post {
				val width = root.imageApplication.width
				val height = root.imageApplication.height
				root.imageApplication.setImageBitmap(
					item.getImageBitmap(
						root.root.context,
						width,
						height
					)
				)
			}
		} else {
			root.imageApplication.setImageResource(item.icon)
		}
		root.imageToggle.isVisible = item.checked

		root.root.setOnClickListener {
			root.imageToggle.isVisible = !root.imageToggle.isVisible
			item.checked = !item.checked
			onChangeToggleListener?.invoke(list.value!!)
		}
	}
}