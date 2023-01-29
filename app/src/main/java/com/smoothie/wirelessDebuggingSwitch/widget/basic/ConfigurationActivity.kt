package com.smoothie.wirelessDebuggingSwitch.widget.basic

import android.content.SharedPreferences
import android.view.View
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.widgetFactory.configuration.WidgetConfigurationActivity
import com.smoothie.wirelessDebuggingSwitch.widget.RoundedWidgetUtilities

class ConfigurationActivity : WidgetConfigurationActivity(
    R.xml.preferences_widget_basic,
    1f
) {

    override fun generateWidget(
        width: Int,
        height: Int,
        widgetPreferences: SharedPreferences
    ): View {
        val view = layoutInflater.inflate(R.layout.widget_switch_small, null)

        RoundedWidgetUtilities.applyPreviewParameters(this, widgetPreferences, view)

        return view
    }

}
