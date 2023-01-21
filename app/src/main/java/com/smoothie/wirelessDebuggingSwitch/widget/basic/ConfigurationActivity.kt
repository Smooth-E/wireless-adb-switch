package com.smoothie.wirelessDebuggingSwitch.widget.basic

import android.content.SharedPreferences
import android.view.View
import android.widget.ImageView
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.Utilities
import com.smoothie.wirelessDebuggingSwitch.core.WidgetConfigurationActivity

class ConfigurationActivity :
    WidgetConfigurationActivity(R.xml.preferences_widget_basic, 1f / 1f) {

    override fun generateWidget(width: Int, height: Int, preferences: SharedPreferences): View {
        val view = layoutInflater.inflate(R.layout.widget_switch_small, null)

        val background = view.findViewById<ImageView>(R.id.background)
        val bitmap = Utilities
            .generateWidgetBackground(this, width.toFloat(), height.toFloat(), preferences)
        background.setImageBitmap(bitmap)

        return view
    }

}
