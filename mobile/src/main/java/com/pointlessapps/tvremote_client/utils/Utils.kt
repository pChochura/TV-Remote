package com.pointlessapps.tvremote_client.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

object Utils {

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
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
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