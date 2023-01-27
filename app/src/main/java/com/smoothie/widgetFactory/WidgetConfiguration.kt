package com.smoothie.widgetFactory

object WidgetConfiguration {

    private const val WIDGET_SHARED_PREFERENCES_NAME_PREFIX = "widget_shared_preferences_"

    fun getWidgetSharedPreferencesName(widgetId: Int): String =
        "${WIDGET_SHARED_PREFERENCES_NAME_PREFIX}$widgetId"

}
