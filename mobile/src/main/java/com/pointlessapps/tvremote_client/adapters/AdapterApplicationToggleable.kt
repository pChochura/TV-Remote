package com.pointlessapps.tvremote_client.adapters

import androidx.core.view.isVisible
import com.pointlessapps.tvremote_client.databinding.ItemApplicationToggleableBinding
import com.pointlessapps.tvremote_client.models.Application

class AdapterApplicationToggleable(apps: List<Application>) :
    AdapterBase<Application, ItemApplicationToggleableBinding>(
        apps.toMutableList(),
        ItemApplicationToggleableBinding::class.java
    ) {

    var onChangeToggleListener: ((List<Application>) -> Unit)? = null

    override fun onBind(root: ItemApplicationToggleableBinding, position: Int) {
        root.imageApplication.setImageResource(list[position].icon)
        root.imageToggle.isVisible = list[position].checked

        root.root.setOnClickListener {
            root.imageToggle.isVisible = !root.imageToggle.isVisible
            list[position].checked = !list[position].checked
            onChangeToggleListener?.invoke(list)
        }
    }

    fun swapItems(from: Int, to: Int) {
        val temp = list[from]
        list[from] = list[to]
        list[to] = temp
        notifyItemMoved(from, to)
    }

    fun list() = list
}