package com.pointlessapps.tvremote_client.utils

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object Utils {

    @Suppress("UNCHECKED_CAST")
    fun getViewModelFactory(activity: Activity, vararg args: Any) =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                modelClass.constructors.first().newInstance(activity, *args) as T
        }
}