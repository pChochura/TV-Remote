package com.pointlessapps.tvremote_client.managers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.pointlessapps.tvremote_client.MainActivity
import com.pointlessapps.tvremote_client.R
import com.pointlessapps.tvremote_client.SplashActivity
import com.pointlessapps.tvremote_client.services.ConnectionService

object NotificationManager {

	private const val CONTENT_INTENT_REQUEST_CODE = 0
	private const val DISCONNECT_INTENT_REQUEST_CODE = 1

	private fun createChannel(context: Context) {
		NotificationManagerCompat.from(context).createNotificationChannel(
			NotificationChannel(
				context.getString(R.string.channel_id),
				context.getString(R.string.channel_name),
				NotificationManager.IMPORTANCE_NONE
			)
		)
	}

	fun createNotification(context: Context): Notification {
		createChannel(context)

		val contentIntent: PendingIntent = PendingIntent.getActivity(
			context,
			CONTENT_INTENT_REQUEST_CODE,
			Intent(context, SplashActivity::class.java).putExtra(MainActivity.DESTINATION, R.id.remote),
			PendingIntent.FLAG_UPDATE_CURRENT
		)

		val disconnectIntent = PendingIntent.getService(
			context,
			DISCONNECT_INTENT_REQUEST_CODE,
			Intent(context, ConnectionService::class.java).putExtra(ConnectionService.DISCONNECT, true),
			PendingIntent.FLAG_UPDATE_CURRENT
		)

		return Notification.Builder(context, context.getString(R.string.channel_id))
			.setContentTitle(context.getString(R.string.notification_title))
			.setContentText(context.getString(R.string.notification_content))
			.setSmallIcon(R.drawable.ic_remote)
			.setContentIntent(contentIntent)
			.addAction(
				Notification.Action.Builder(
					null,
					context.getString(R.string.disconnect),
					disconnectIntent
				).build()
			)
			.setAutoCancel(false)
			.setLocalOnly(true)
			.setOngoing(true)
			.build()
	}
}