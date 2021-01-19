package com.pointlessapps.tvremote_client.fragments

import androidx.fragment.app.viewModels
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelDeviceDiscovery

class FragmentDeviceDiscovery : FragmentBaseImpl() {

    private val viewModel by viewModels<ViewModelDeviceDiscovery> {
        Utils.getViewModelFactory(requireActivity(), root())
    }

    override fun getLayoutId() = R.layout.fragment_device_discovery

    override fun created() {
        viewModel.onChangeFragmentListener = onChangeFragment
        viewModel.setDeviceListener()
        viewModel.loadDeviceInfo()
        viewModel.startDiscovery()
        viewModel.setDeviceList()
        viewModel.setClickListeners()
    }
}