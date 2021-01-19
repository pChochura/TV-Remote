package com.pointlessapps.tvremote_client.adapters

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.pointlessapps.tvremote_client.R

class AdapterDevice(devices: MutableList<DeviceInfo>) : AdapterBase<DeviceInfo>(devices) {

    override fun getLayoutId(viewType: Int) = R.layout.item_device

    override fun onBind(root: View, position: Int) {
        root.findViewById<AppCompatTextView>(R.id.textName).text = list[position].name
        root.findViewById<AppCompatTextView>(R.id.textAddress).text = list[position].uri.host

        root.setOnClickListener { onClickListener?.invoke(list[position]) }
    }
}