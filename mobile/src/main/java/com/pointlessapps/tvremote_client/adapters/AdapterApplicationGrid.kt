package com.pointlessapps.tvremote_client.adapters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pointlessapps.tvremote_client.databinding.ItemApplicationGridBinding
import com.pointlessapps.tvremote_client.databinding.ItemApplicationListBinding
import com.pointlessapps.tvremote_client.models.Application

class AdapterApplicationGrid(apps: LiveData<List<Application>>) :
	AdapterBase<Application, ItemApplicationGridBinding>(
		apps as MutableLiveData,
		ItemApplicationGridBinding::inflate
	) {

	init {
		setHasStableIds(true)
	}

	override fun onBind(root: ItemApplicationGridBinding, item: Application) {
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