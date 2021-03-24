package com.pointlessapps.tvremote_client.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.pointlessapps.tvremote_client.utils.InflateMethod

abstract class FragmentBase<Binding : ViewBinding>(private val inflateMethod: InflateMethod<Binding>) :
	Fragment() {

	abstract fun created()
	open fun refreshed() = Unit

	var onDispatchKeyEvent: ((KeyEvent) -> Boolean)? = null

	private var _root: Binding? = null
	protected val root: Binding
		get() = _root!!

	protected open var forceRefresh = false
		get() = field.also { field = false }

	fun forceRefresh() {
		forceRefresh = true
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		if (_root == null || forceRefresh) {
			_root = inflateMethod(inflater, container, false)

			created()
		} else {
			refreshed()
		}

		return _root?.root
	}

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