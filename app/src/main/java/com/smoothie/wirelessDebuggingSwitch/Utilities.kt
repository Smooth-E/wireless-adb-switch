package com.smoothie.wirelessDebuggingSwitch

import android.content.Context

class Utilities {

    companion object {

        private const val WIDGET_SHARED_PREFERENCES_NAME_PREFIX = "widget_shared_preferences_"

        fun dp2px(context: Context, dp: Float): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }

        fun getWidgetSharedPreferencesName(widgetId: Int): String =
            "${WIDGET_SHARED_PREFERENCES_NAME_PREFIX}$widgetId"

        fun getSharedPreferencesKey(context: Context, id: Int): String =
            context.getString(id)

    }

}