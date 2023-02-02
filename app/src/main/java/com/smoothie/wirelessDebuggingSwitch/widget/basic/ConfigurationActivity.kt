package com.smoothie.wirelessDebuggingSwitch.widget.basic

import android.content.SharedPreferences
import android.view.View
import android.widget.TextView
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.widgetFactory.configuration.WidgetConfigurationActivity
import com.smoothie.wirelessDebuggingSwitch.PreferenceUtilities
import com.smoothie.wirelessDebuggingSwitch.widget.RoundedWidgetUtilities

class ConfigurationActivity : WidgetConfigurationActivity(
    R.xml.widget_basic_preferences,
    1f
) {

    override fun generateWidget(
        width: Int,
        height: Int,
        widgetPreferences: SharedPreferences
    ): View {
        val view = layoutInflater.inflate(R.layout.widget_switch_small, null)

        RoundedWidgetUtilities.applyPreviewParameters(this, widgetPreferences, view)

        val textColor = PreferenceUtilities.getLightOrDarkTextColor(this, widgetPreferences)
        view.findViewById<TextView>(R.id.text_view_name).setTextColor(textColor)

        return view
    }

}
