package com.pointlessapps.tvremote_client.fragments

import android.content.Context
import com.pointlessapps.tvremote_client.databinding.FragmentDevicePairingBinding
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelDevice
import com.pointlessapps.tvremote_client.viewModels.ViewModelDevicePairing

class FragmentDevicePairing :
    FragmentBaseImpl<FragmentDevicePairingBinding>(FragmentDevicePairingBinding::class.java) {

    private lateinit var deviceWrapper: DeviceWrapper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        deviceWrapper = Utils.getViewModel(ViewModelDevice::class.java, activity()).deviceWrapper
    }

    override fun created() {
        val viewModel = Utils.getViewModel(
            ViewModelDevicePairing::class.java,
            activity(),
            this,
            root(),
            deviceWrapper
        )

        viewModel.setKeyboard()
        viewModel.setDispatchKeyEventListener()
        viewModel.onPopBackStackListener = onPopBackStack
        onDispatchKeyEvent = { viewModel.onDispatchKeyEventListener?.invoke(it) ?: false }
    }
}