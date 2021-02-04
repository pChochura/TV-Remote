package com.pointlessapps.tvremote_client.fragments

import android.animation.Animator
import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.pointlessapps.tvremote_client.R

abstract class FragmentBaseImpl<T : ViewBinding>(private val bindingClass: Class<T>) : Fragment(),
    FragmentBase<T> {

    private var rootView: T? = null
    override fun root() = rootView!!

    fun activity() = requireActivity() as AppCompatActivity

    override var forceRefresh = false
    override var onChangeFragment: ((FragmentBase<*>) -> Unit)? = null
    override var onPopBackStack: (() -> Unit)? = null
    override var onDispatchKeyEvent: ((KeyEvent) -> Boolean)? = null
    override var onPauseActivity: (() -> Unit)? = null
    override var onResumeActivity: (() -> Unit)? = null

    abstract override fun created()

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null || forceRefresh) {
            rootView = bindingClass.getMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            ).invoke(null, inflater, container, false) as T

            created()
        } else {
            refreshed()
        }

        return rootView?.root
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? =
        AnimatorInflater.loadAnimator(
            requireContext(),
            if (enter) {
                R.anim.fade_in
            } else {
                R.anim.fade_out
            }
        )

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int) =
        runCatching {
            AnimationUtils.loadAnimation(
                requireContext(),
                transit
            )
        }.getOrElse {
            runCatching {
                AnimationUtils.loadAnimation(
                    requireContext(),
                    nextAnim
                )
            }.getOrNull()
        }
}