package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color

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
            preferences: SharedPreferences
        ): ColorStateList {

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
            return ColorStateList.valueOf(colorInt)
        }

    }

}