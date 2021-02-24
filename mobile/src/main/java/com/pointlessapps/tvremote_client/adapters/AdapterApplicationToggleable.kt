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
        root.imageToggle.isVisible = list[position].checked

        root.root.setOnClickListener {
            root.imageToggle.isVisible = !root.imageToggle.isVisible
            list[position].checked = !list[position].checked
            onChangeToggleListener?.invoke(list)
        }
    }

    fun updateList(list: List<Application>) {
        this.list.apply {
            clear()
            addAll(list)
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