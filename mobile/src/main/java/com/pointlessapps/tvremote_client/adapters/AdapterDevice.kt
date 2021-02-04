package com.pointlessapps.tvremote_client.adapters

import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.pointlessapps.tvremote_client.databinding.ItemDeviceBinding

class AdapterDevice(devices: MutableList<DeviceInfo>) :
    AdapterBase<DeviceInfo, ItemDeviceBinding>(devices, ItemDeviceBinding::class.java) {

    override fun onBind(root: ItemDeviceBinding, position: Int) {
        root.textName.text = list[position].name
        root.textAddress.text = list[position].uri.host
    }
}