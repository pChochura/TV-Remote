package com.pointlessapps.tvremote_client.adapters

import com.pointlessapps.tvremote_client.databinding.ItemApplicationBinding
import com.pointlessapps.tvremote_client.models.Application

class AdapterApplication(apps: List<Application>) :
    AdapterBase<Application, ItemApplicationBinding>(
        apps.toMutableList(),
        ItemApplicationBinding::class.java
    ) {

    override fun onBind(root: ItemApplicationBinding, position: Int) {
        if (list[position].icon == 0) {
            root.imageApplication.post {
                val width = root.imageApplication.width
                val height = root.imageApplication.height
                root.imageApplication.setImageBitmap(
                    list[position].getImageBitmap(
                        root.root.context,
                        width,
                        height
                    )
                )
            }
        } else {
            root.imageApplication.setImageResource(list[position].icon)
        }
    }
}