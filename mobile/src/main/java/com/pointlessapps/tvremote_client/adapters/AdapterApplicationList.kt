package com.pointlessapps.tvremote_client.adapters

import androidx.lifecycle.MutableLiveData
import com.pointlessapps.tvremote_client.databinding.ItemApplicationListBinding
import com.pointlessapps.tvremote_client.models.Application

class AdapterApplicationList(apps: List<Application>) :
	AdapterBase<Application, ItemApplicationListBinding>(
		MutableLiveData(apps),
		ItemApplicationListBinding::inflate
	) {

	init {
		setHasStableIds(true)
	}

	override fun onBind(root: ItemApplicationListBinding, item: Application) {
		if (item.icon == 0) {
			root.imageApplication.post {
				val width = root.imageApplication.width
				val height = root.imageApplication.height
				root.imageApplication.setImageBitmap(
					Application.getImageBitmap(
						item.activityName,
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