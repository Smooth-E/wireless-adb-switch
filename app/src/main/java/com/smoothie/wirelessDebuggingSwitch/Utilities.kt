package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*

object Utilities {

    private const val WIDGET_SHARED_PREFERENCES_NAME_PREFIX = "widget_shared_preferences_"

    fun getWidgetSharedPreferencesName(widgetId: Int): String =
        "${WIDGET_SHARED_PREFERENCES_NAME_PREFIX}$widgetId"

    fun getSharedPreferencesKey(context: Context, id: Int): String =
        context.getString(id)

    fun dp2px(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun createPaint(context: Context, preferences: SharedPreferences): Paint {
        var key = context.getString(R.string.key_use_colorful_background)
        val useColorfulBackground =
            preferences.getBoolean(key, false)

        key = context.getString(R.string.key_background_transparency)
        val transparency =
            preferences.getInt(key, 100) / 100f

        val colorId =
            if (useColorfulBackground)
                R.color.colorGoogleWidgetBackground
            else
                R.color.colorSurface

        val theme = context.theme
        val color = Color.valueOf(theme.resources.getColor(colorId, theme))
        val colorInt =  Color.argb(transparency, color.red(), color.green(), color.blue())

        val paint = Paint()
        paint.color = colorInt
        paint.style = Paint.Style.FILL
        paint.blendMode = BlendMode.SRC

        return paint
    }

    fun generateWidgetCornerBitmap(
        context: Context,
        preferences: SharedPreferences,
        radius: Int
    ): Bitmap {
        val paint = createPaint(context, preferences)
        val bitmap = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val radiusFloat = radius.toFloat()
        canvas.drawCircle(radiusFloat, radiusFloat, radiusFloat, paint)

        return bitmap
    }

    fun generateRectangleBitmapForWidget(
        context: Context,
        preferences: SharedPreferences
    ): Bitmap {
        val paint = createPaint(context, preferences)
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawRect(0f, 0f, 512f, 512f, paint)

        return bitmap
    }

    fun getWidgetCornerRadius(
        context: Context,
        preferences: SharedPreferences
    ): Int {
        val key = context.getString(R.string.key_corner_roundness)
        val roundnessModifier = preferences.getInt(key, 100) / 100f
        val systemRoundness =
            context.resources.getDimensionPixelSize(R.dimen.system_appwidget_background_radius)
        val radius = (systemRoundness * roundnessModifier).toInt()
        return if (radius == 0) 1 else radius
    }

    fun getDimen(context: Context, resource: Int): Int =
        context.resources.getDimensionPixelSize(resource)

    fun getInnerCornerRadius(context: Context): Int =
        getDimen(context, R.dimen.system_appwidget_inner_radius)

    fun getBackgroundCornerRadius(context: Context): Int =
        getDimen(context, R.dimen.system_appwidget_background_radius)

}
