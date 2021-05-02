package com.pointlessapps.tvremote_client.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

object Utils {

	fun vibrate(context: Context) {
		context.getSystemService(Vibrator::class.java)
			.vibrate(
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
				} else {
					VibrationEffect.createOneShot(50, 50)
				}
			)
	}

	fun showKeyboard(context: Context, view: View) {
		view.post { view.requestFocus() }
		context.getSystemService(InputMethodManager::class.java)
			.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
	}

	fun hideKeyboard(context: Context, view: View) {
		context.getSystemService(InputMethodManager::class.java)
			.hideSoftInputFromWindow(view.windowToken, 0)
	}

	fun setOnKeyboardChangeVisibilityListener(
		view: View,
		onChangeVisibilityListener: (Boolean) -> Unit
	) = view.viewTreeObserver.addOnGlobalLayoutListener {
		val rect = Rect().also { view.getWindowVisibleDisplayFrame(it) }
		onChangeVisibilityListener(view.rootView.height - (rect.bottom - rect.top) > 100)
	}

	@Suppress("DEPRECATION")
	fun toggleShowOnLockScreen(activity: Activity, enabled: Boolean) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
			activity.setShowWhenLocked(enabled)
		} else {
			if (enabled) {
				activity.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
			} else {
				activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
			}
		}
	}
}