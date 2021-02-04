package com.pointlessapps.tvremote_client.fragments

import android.view.KeyEvent
import androidx.viewbinding.ViewBinding

interface FragmentBase<T : ViewBinding> {

    fun created() = Unit
    fun refreshed() = Unit

    fun root(): T? = null

    var forceRefresh: Boolean

    var onChangeFragment: ((FragmentBase<*>) -> Unit)?
    var onPopBackStack: (() -> Unit)?
    var onDispatchKeyEvent: ((KeyEvent) -> Boolean)?
    var onPauseActivity: (() -> Unit)?
    var onResumeActivity: (() -> Unit)?
}