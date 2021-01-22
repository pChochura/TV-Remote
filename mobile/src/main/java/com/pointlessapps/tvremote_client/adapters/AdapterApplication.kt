package com.pointlessapps.tvremote_client.adapters

import android.view.View
import com.pointlessapps.tvremote_client.R
import kotlinx.android.synthetic.main.item_application.view.*

class AdapterApplication(apps: List<Pair<Int, String>>) :
    AdapterBase<Pair<Int, String>>(apps.toMutableList()) {

    override fun getLayoutId(viewType: Int) = R.layout.item_application

    override fun onBind(root: View, position: Int) {
        root.imageApplication.setImageResource(list[position].first)

        root.setOnClickListener { onClickListener?.invoke(list[position]) }
    }
}