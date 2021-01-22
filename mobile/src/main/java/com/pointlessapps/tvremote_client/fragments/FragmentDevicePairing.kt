package com.pointlessapps.tvremote_client.fragments

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelDevice
import com.pointlessapps.tvremote_client.viewModels.ViewModelDevicePairing

class FragmentDevicePairing : FragmentBaseImpl() {

    private lateinit var deviceWrapper: DeviceWrapper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        deviceWrapper = ViewModelProvider(
            activity(),
            Utils.getViewModelFactory(activity())
        ).get(ViewModelDevice::class.java).deviceWrapper
    }

    override fun getLayoutId() = R.layout.fragment_device_pairing

    override fun created() {
        val viewModel =
            ViewModelProvider(
                this,
                Utils.getViewModelFactory(
                    activity(),
                    root(),
                    deviceWrapper
                )
            ).get(ViewModelDevicePairing::class.java)

        viewModel.setKeyboard()
        viewModel.setDispatchKeyEventListener()
        viewModel.onPopBackStackListener = onPopBackStack
        onDispatchKeyEvent = { viewModel.onDispatchKeyEventListener?.invoke(it) ?: false }
    }
}