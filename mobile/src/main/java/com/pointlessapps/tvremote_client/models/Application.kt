package com.pointlessapps.tvremote_client.models

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.TextPaint
import android.util.TypedValue
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.pointlessapps.tvremote_client.R

data class Application(
    @DrawableRes val icon: Int = 0,
    val packageName: String,
    val activityName: String,
    var checked: Boolean = true
) {
    fun getImageBitmap(context: Context, width: Int, height: Int): Bitmap {
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
        val x = width * 0.5f
        val y = (height - paint.descent() - paint.ascent()) * 0.5f
        canvas.drawText(activityName, x, y, paint)

        return bitmap
    }
}