package com.smoothie.wirelessDebuggingSwitch.widget.basic

import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.Utilities
import com.smoothie.wirelessDebuggingSwitch.core.WidgetConfigurationActivity

class ConfigurationActivity :
    WidgetConfigurationActivity(R.xml.preferences_widget_basic, 2f / 1f) {

    override fun generateWidget(preferences: SharedPreferences): View {
        val view = layoutInflater.inflate(R.layout.widget_switch_small, null)

        val background = view.findViewById<ViewGroup>(R.id.clickable)
        background.backgroundTintList = Utilities.generateWidgetBackground(this, preferences)

        return view
    }

}