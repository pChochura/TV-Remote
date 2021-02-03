package com.pointlessapps.tvremote_client.models

import androidx.annotation.DrawableRes

data class Application(
    @DrawableRes val icon: Int,
    val packageName: String,
    val activityName: String,
    var checked: Boolean = true
)