package com.pointlessapps.tvremote_client.utils

import android.animation.ObjectAnimator
import android.app.Application
import android.content.*
import android.os.IBinder
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.github.kittinunf.fuel.core.Request

fun Request.string(callback: (String?) -> Any) =
	responseString { _, _, (body, _) -> callback(body) }

fun View.scaleAnimation() {
	arrayOf("scaleX", "scaleY").forEach {
		ObjectAnimator.ofFloat(this, it, 0.9f, 1.1f, 1.0f).apply {
			repeatCount = ObjectAnimator.INFINITE
			repeatMode = ObjectAnimator.REVERSE
			duration = 2000
			interpolator = AccelerateDecelerateInterpolator()
			start()
		}
	}
}

inline fun <reified T : IBinder> ContextWrapper.bindService(
	destination: Class<*>,
	crossinline onBoundCallback: (binder: T?) -> Unit
) = bindService(
	Intent(this, destination), object : ServiceConnection {
		override fun onServiceDisconnected(name: ComponentName?) = Unit
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) =
			onBoundCallback(service as? T?)
	},
	Context.BIND_AUTO_CREATE
)