package com.pointlessapps.tvremote_client.utils

import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.math.abs

class SwipeTouchHandler(private val onActionListener: (ACTION) -> Unit) : View.OnTouchListener {

	enum class ACTION {
		LONG_CLICK, CLICK, SWIPE_LEFT, SWIPE_RIGHT, SWIPE_UP, SWIPE_DOWN
	}

	companion object {
		const val LONG_CLICK_TIME = 500L
		const val CLICK_POSITION_THRESHOLD = 50f
	}

	private var startPos = 0f to 0f
	private var startTime = 0L
	private val longClickTimer = Timer()
	private val getLongClickTask = {
		object : TimerTask() {
			override fun run() {
				onActionListener(ACTION.LONG_CLICK)
			}
		}
	}
	private var longClickTask = getLongClickTask()

	override fun onTouch(v: View, event: MotionEvent): Boolean {
		when (event.action) {
			MotionEvent.ACTION_CANCEL -> {
				longClickTask.cancel()
				longClickTask = getLongClickTask()
			}
			MotionEvent.ACTION_MOVE -> {
				val xDiff = event.x - startPos.first
				val yDiff = event.y - startPos.second
				if (abs(xDiff) >= CLICK_POSITION_THRESHOLD && abs(yDiff) >= CLICK_POSITION_THRESHOLD) {
					longClickTask.cancel()
					longClickTask = getLongClickTask()
				}
			}
			MotionEvent.ACTION_DOWN -> {
				startPos = event.x to event.y
				startTime = System.currentTimeMillis()
				longClickTimer.schedule(longClickTask, LONG_CLICK_TIME)
			}
			MotionEvent.ACTION_UP -> {
				longClickTask.cancel()
				longClickTask = getLongClickTask()
				val xDiff = event.x - startPos.first
				val yDiff = event.y - startPos.second
				if (abs(xDiff) < CLICK_POSITION_THRESHOLD && abs(yDiff) < CLICK_POSITION_THRESHOLD) {
					if (System.currentTimeMillis() - startTime <= LONG_CLICK_TIME) {
						v.performClick()
						onActionListener(ACTION.CLICK)
						return true
					}

					return false
				}
				val action = if (abs(xDiff) > abs(yDiff)) {
					if (xDiff > 0) ACTION.SWIPE_RIGHT else ACTION.SWIPE_LEFT
				} else {
					if (yDiff > 0) ACTION.SWIPE_DOWN else ACTION.SWIPE_UP
				}
				onActionListener(action)
			}
		}

		return true
	}
}