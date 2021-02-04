package com.pointlessapps.tvremote_client.adapters

import com.pointlessapps.tvremote_client.databinding.ItemApplicationBinding
import com.pointlessapps.tvremote_client.models.Application

class AdapterApplication(apps: List<Application>) :
    AdapterBase<Application, ItemApplicationBinding>(
        apps.toMutableList(),
        ItemApplicationBinding::class.java
    ) {

    override fun onBind(root: ItemApplicationBinding, position: Int) {
        root.imageApplication.setImageResource(list[position].icon)
    }
}