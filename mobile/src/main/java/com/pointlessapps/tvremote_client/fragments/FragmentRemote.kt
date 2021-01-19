package com.pointlessapps.tvremote_client.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.models.DeviceWrapper
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelRemote

class FragmentRemote : FragmentBaseImpl() {

    companion object {
        fun newInstance(deviceWrapper: DeviceWrapper) =
            FragmentRemote().apply {
                arguments = Bundle().apply {
                    putSerializable("deviceWrapper", deviceWrapper)
                }
            }
    }

    private lateinit var viewModel: ViewModelRemote
    private lateinit var deviceWrapper: DeviceWrapper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        deviceWrapper = arguments?.getSerializable("deviceWrapper") as DeviceWrapper
    }

    override fun getLayoutId() = R.layout.fragment_remote

    override fun created() {
        viewModel = ViewModelProvider(
            this,
            Utils.getViewModelFactory(requireActivity(), root(), deviceWrapper)
        ).get(ViewModelRemote::class.java)

        viewModel.setOnDisconnectedListener()
        viewModel.powerOn()
        viewModel.setTouchHandler()
        viewModel.setClickListeners()
        viewModel.setApplicationsList()
        onDispatchKeyEvent = { viewModel.onDispatchKeyEventListener?.invoke(it) ?: false }
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.isConnected()) {
            viewModel.reconnect()
        }
    }
}