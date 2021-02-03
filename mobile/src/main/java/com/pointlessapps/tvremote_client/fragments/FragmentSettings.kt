package com.pointlessapps.tvremote_client.fragments

import androidx.lifecycle.ViewModelProvider
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelSettings

class FragmentSettings : FragmentBaseImpl() {

    override fun getLayoutId() = R.layout.fragment_settings

    override fun created() {
        val viewModel = ViewModelProvider(
            this,
            Utils.getViewModelFactory(activity(), root())
        ).get(ViewModelSettings::class.java)

        viewModel.prepareSettings()
        viewModel.prepareClickListeners()
    }
}