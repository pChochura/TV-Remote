package com.pointlessapps.tvremote_client.managers

import android.view.KeyEvent
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.fragments.FragmentBase

class FragmentManager private constructor(
    private val fragmentManager: androidx.fragment.app.FragmentManager,
    private var currentFragment: FragmentBase
) {

    @IdRes
    private var containerId: Int? = null

    companion object {
        fun of(activity: FragmentActivity, fragment: FragmentBase) =
            FragmentManager(activity.supportFragmentManager, fragment)
    }

    init {
        prepareFragment(currentFragment)
    }

    private fun prepareFragment(fragment: FragmentBase) {
        fragment.onChangeFragment = { setFragment(it.apply { prepareFragment(this) }) }
        fragment.onPopBackStack = { fragmentManager.popBackStack() }
    }

    fun showIn(@IdRes containerId: Int) {
        this.containerId = containerId
        fragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            add(containerId, currentFragment as Fragment)
            commit()
        }
    }

    private fun setFragment(fragment: FragmentBase) {
        if (containerId === null) {
            throw NullPointerException("Fragment container cannot be null.")
        }

        if (fragment === currentFragment) {
            return
        }

        currentFragment = fragment

        fragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            replace(containerId!!, fragment as Fragment)
            addToBackStack(null)
            commit()
        }
    }

    fun dispatchKeyEvent(event: KeyEvent) =
        currentFragment.onDispatchKeyEvent?.invoke(event)
}
