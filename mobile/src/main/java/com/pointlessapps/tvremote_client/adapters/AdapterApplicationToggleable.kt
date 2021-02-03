package com.pointlessapps.tvremote_client.adapters

import android.view.View
import androidx.core.view.isVisible
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.models.Application
import kotlinx.android.synthetic.main.item_application.view.imageApplication
import kotlinx.android.synthetic.main.item_application_toggleable.view.*

class AdapterApplicationToggleable(apps: List<Application>) :
    AdapterBase<Application>(apps.toMutableList()) {

    var onChangeToggleListener: ((List<Application>) -> Unit)? = null

    override fun getLayoutId(viewType: Int) = R.layout.item_application_toggleable

    override fun onBind(root: View, position: Int) {
        root.imageApplication.setImageResource(list[position].icon)
        root.imageToggle.isVisible = list[position].checked

        root.setOnClickListener {
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