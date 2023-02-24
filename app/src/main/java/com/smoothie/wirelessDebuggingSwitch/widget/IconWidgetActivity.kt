package com.smoothie.wirelessDebuggingSwitch.widget

import android.content.SharedPreferences
import android.view.View
import com.smoothie.widgetFactory.configuration.WidgetConfigurationActivity
import com.smoothie.wirelessDebuggingSwitch.R

class IconWidgetActivity : WidgetConfigurationActivity(
    R.xml.widget_icon_preferences,
    1f
) {

    override fun generateWidget(
        width: Int,
        height: Int,
        widgetPreferences: SharedPreferences
    ): View {
        TODO("Not yet implemented")
    }

}