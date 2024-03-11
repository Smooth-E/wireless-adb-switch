package com.smoothie.widgetFactory

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.ColorInt

class WidgetCutoutDrawable(
    private val cornerRadius: Float,
    private val padding: Float,
    @ColorInt private val color: Int
) : Drawable() {

    override fun draw(canvas: Canvas) {
        val path = Path()
        val right = bounds.width() - padding
        val bottom = bounds.height()- padding
        val direction = Path.Direction.CW
        path.addRoundRect(padding, padding, right, bottom, cornerRadius, cornerRadius,  direction)
        canvas.clipOutPath(path)

        val paint = Paint()
        Log.d("DrawableThing", color.toString())
        paint.color = color
        canvas.drawRect(Rect(0, 0, bounds.width(), bounds.height()), paint)
    }

    override fun setAlpha(alpha: Int) {
        TODO("Not yet implemented")
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        TODO("Not yet implemented")
    }

    override fun getOpacity(): Int =
        PixelFormat.OPAQUE

}
