package com.pointlessapps.tvremote_client.fragments

import com.pointlessapps.tvremote_client.databinding.FragmentDeviceDiscoveryBinding
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelDeviceDiscovery

class FragmentDeviceDiscovery :
    FragmentBaseImpl<FragmentDeviceDiscoveryBinding>(FragmentDeviceDiscoveryBinding::class.java) {

    private lateinit var viewModel: ViewModelDeviceDiscovery

    override fun created() {
        viewModel =
            Utils.getViewModel(ViewModelDeviceDiscovery::class.java, activity(), this, root())

        onPauseActivity = { viewModel.onPauseActivityListener?.invoke() }
        onResumeActivity = { viewModel.onResumeActivityListener?.invoke() }
        viewModel.onChangeFragmentListener = onChangeFragment
        viewModel.setDeviceListener()
        viewModel.loadDeviceInfo()
        viewModel.startDiscovery()
        viewModel.setDeviceList()
        viewModel.setClickListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.setDeviceListener()
        viewModel.loadDeviceInfo()
        viewModel.startDiscovery()
        viewModel.setDeviceList()
    }
}