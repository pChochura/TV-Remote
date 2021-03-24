package com.pointlessapps.tvremote_client.adapters

import androidx.lifecycle.MutableLiveData
import com.pointlessapps.tvremote_client.databinding.ItemApplicationBinding
import com.pointlessapps.tvremote_client.models.Application

class AdapterApplication(apps: List<Application>) :
	AdapterBase<Application, ItemApplicationBinding>(
		MutableLiveData(apps),
		ItemApplicationBinding::inflate
	) {

	init {
		setHasStableIds(true)
	}

	override fun onBind(root: ItemApplicationBinding, item: Application) {
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
	}
}