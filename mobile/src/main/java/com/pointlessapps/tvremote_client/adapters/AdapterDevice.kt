package com.pointlessapps.tvremote_client.adapters

import androidx.lifecycle.MutableLiveData
import com.google.android.tv.support.remote.discovery.DeviceInfo
import com.pointlessapps.tvremote_client.databinding.ItemDeviceBinding

class AdapterDevice(devices: List<DeviceInfo>) :
	AdapterBase<DeviceInfo, ItemDeviceBinding>(MutableLiveData(devices), ItemDeviceBinding::inflate) {

	init {
		setHasStableIds(true)
	}

	override fun onBind(root: ItemDeviceBinding, item: DeviceInfo) {
		root.textName.text = item.name
		root.textAddress.text = item.uri.host
	}
}