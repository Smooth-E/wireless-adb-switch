package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.util.Log

class Utilities {

    companion object {

        private const val WIDGET_SHARED_PREFERENCES_NAME_PREFIX = "widget_shared_preferences_"

        fun getWidgetSharedPreferencesName(widgetId: Int): String =
            "${WIDGET_SHARED_PREFERENCES_NAME_PREFIX}$widgetId"

        fun getSharedPreferencesKey(context: Context, id: Int): String =
            context.getString(id)

        fun dp2px(context: Context, dp: Float): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }

        fun generateWidgetBackground(
            context: Context,
            width: Float,
            height: Float,
            preferences: SharedPreferences
        ): Bitmap {

            val radius = getBackgroundCornerRadius(context).toFloat()

            var key = context.getString(R.string.key_use_colorful_background)
            val useColorfulBackground =
                preferences.getBoolean(key, false)

            key = context.getString(R.string.key_background_transparency)
            val transparency =
                preferences.getInt(key, 100) / 100f

            val colorId =
                if (useColorfulBackground)
                    R.color.colorPrimaryContainer
                else
                    R.color.colorSurface

            val theme = context.theme
            val color = Color.valueOf(theme.resources.getColor(colorId, theme))
            val colorInt =  Color.argb(transparency, color.red(), color.green(), color.blue())

            val paint = Paint()
            paint.color = colorInt
            paint.style = Paint.Style.FILL
            paint.blendMode = BlendMode.SRC

            Log.d("Drawing", "Creating bitmap of $width x $height")

            val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Canvas origin is located in the upper left corner

            // Draw corners
            canvas.drawCircle(radius, radius, radius, paint)
            canvas.drawCircle(radius, height - radius, radius, paint)
            canvas.drawCircle(width - radius, radius, radius, paint)
            canvas.drawCircle(width - radius, height - radius, radius, paint)

            // Draw center piece
            canvas.drawRect(radius, radius, width - radius, height - radius, paint)

            // Draw side pieces
            canvas.drawRect(0f, radius, radius, height - radius, paint)
            canvas.drawRect(radius, 0f, width - radius, radius, paint)
            canvas.drawRect(width - radius, radius, width, height - radius, paint)
            canvas.drawRect(radius, height - radius, width - radius, height, paint)

            return bitmap
        }

        fun getDimen(context: Context, resource: Int): Int =
            context.resources.getDimensionPixelSize(resource)

        fun getInnerCornerRadius(context: Context): Int =
            getDimen(context, R.dimen.system_appwidget_inner_radius)

        fun getBackgroundCornerRadius(context: Context): Int =
            getDimen(context, R.dimen.system_appwidget_background_radius)

    }

}