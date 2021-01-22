package com.pointlessapps.tvremote_client.fragments

import android.view.KeyEvent
import android.view.ViewGroup
import androidx.annotation.LayoutRes

interface FragmentBase {
    @LayoutRes
    fun getLayoutId(): Int

    fun created() = Unit
    fun refreshed() = Unit

    fun root(): ViewGroup? = null

    var forceRefresh: Boolean

    var onChangeFragment: ((FragmentBase) -> Unit)?
    var onPopBackStack: (() -> Unit)?
    var onDispatchKeyEvent: ((KeyEvent) -> Boolean)?
    var onPauseActivity: (() -> Unit)?
    var onResumeActivity: (() -> Unit)?
}