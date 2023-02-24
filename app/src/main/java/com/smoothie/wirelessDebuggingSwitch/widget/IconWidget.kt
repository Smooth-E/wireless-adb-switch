package com.smoothie.wirelessDebuggingSwitch.widget

import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews

class IconWidget : SwitchWidget(IconWidget::class.java.name) {

    override fun generateRemoteViews(
        context: Context,
        widgetId: Int,
        preferences: SharedPreferences
    ): RemoteViews {
        TODO("Not yet implemented")
    }

}