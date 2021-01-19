package com.pointlessapps.tvremote_client.fragments

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelDevicePairing

class FragmentDevicePairing : FragmentBaseImpl() {

    private lateinit var deviceWrapper: DeviceWrapper

    companion object {
        fun newInstance(deviceWrapper: DeviceWrapper) =
            FragmentDevicePairing().apply {
                arguments = Bundle().apply {
                    putSerializable("deviceWrapper", deviceWrapper)
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        deviceWrapper = arguments?.getSerializable("deviceWrapper") as DeviceWrapper
    }

    override fun getLayoutId() = R.layout.fragment_device_pairing

    override fun created() {
        val viewModel =
            ViewModelProvider(
                this,
                Utils.getViewModelFactory(requireActivity(), root(), deviceWrapper)
            ).get(ViewModelDevicePairing::class.java)

        viewModel.setKeyboard()
        viewModel.setDispatchKeyEventListener()
        viewModel.onPopBackStackListener = onPopBackStack
        onDispatchKeyEvent = { viewModel.onDispatchKeyEventListener?.invoke(it) ?: false }
    }
}