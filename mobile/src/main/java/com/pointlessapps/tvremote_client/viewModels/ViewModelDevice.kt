package com.pointlessapps.tvremote_client.viewModels

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import com.pointlessapps.tvremote_client.models.DeviceWrapper

class ViewModelDevice(activity: AppCompatActivity) :
    AndroidViewModel(activity.application) {
    lateinit var deviceWrapper: DeviceWrapper
}