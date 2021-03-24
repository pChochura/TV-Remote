package com.pointlessapps.tvremote_client.utils

import android.animation.ObjectAnimator
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