package com.pointlessapps.tvremote_client.models

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.pointlessapps.tvremote_client.R
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Application(
	@DrawableRes val icon: Int = 0,
	val packageName: String,
	val activityName: String,
	var checked: Boolean = true,
	val id: Long = UUID.randomUUID().hashCode().toLong(),
) {
	companion object {
		fun getImageBitmap(text: String, context: Context, width: Int, height: Int): Bitmap {
			val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
			val canvas = Canvas(bitmap)
			canvas.drawColor(ContextCompat.getColor(context, R.color.primaryDark))
			canvas.clipPath(Path().apply {
				addRoundRect(
					0f,
					0f,
					width.toFloat(),
					height.toFloat(),
					5f,
					5f,
					Path.Direction.CW
				)
			})
			val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
				typeface = context.resources.getFont(R.font.montserrat_bold)
				color = ContextCompat.getColor(context, R.color.textPrimary)
				textAlign = Paint.Align.CENTER
				textSize = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_SP,
					18f,
					Resources.getSystem().displayMetrics
				)
			}
			val layout = StaticLayout.Builder.obtain(
				text, 0, text.length, paint,
				(width * 0.9f).toInt()
			).setAlignment(Layout.Alignment.ALIGN_NORMAL)
				.setMaxLines(3)
				.setEllipsize(TextUtils.TruncateAt.END)
				.build()
			val x = width * 0.5f
			val y = (height - layout.height) * 0.5f
			canvas.translate(x, y)
			layout.draw(canvas)

			return bitmap
		}
	}
}