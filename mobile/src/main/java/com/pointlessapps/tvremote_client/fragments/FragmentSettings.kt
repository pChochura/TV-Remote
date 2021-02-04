package com.pointlessapps.tvremote_client.fragments

import com.pointlessapps.tvremote_client.databinding.FragmentSettingsBinding
import com.pointlessapps.tvremote_client.utils.Utils
import com.pointlessapps.tvremote_client.viewModels.ViewModelSettings

class FragmentSettings :
    FragmentBaseImpl<FragmentSettingsBinding>(FragmentSettingsBinding::class.java) {

    override fun created() {
        val viewModel = Utils.getViewModel(ViewModelSettings::class.java, activity(), this, root())

        viewModel.prepareSettings()
        viewModel.prepareClickListeners()
    }
}