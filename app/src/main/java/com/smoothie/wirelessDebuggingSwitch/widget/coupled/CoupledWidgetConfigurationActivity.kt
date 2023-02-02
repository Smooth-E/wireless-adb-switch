package com.smoothie.wirelessDebuggingSwitch.widget.coupled

import android.content.SharedPreferences
import android.view.View
import com.smoothie.widgetFactory.configuration.WidgetConfigurationActivity
import com.smoothie.wirelessDebuggingSwitch.R

class CoupledWidgetConfigurationActivity : WidgetConfigurationActivity(
    CoupledWidget::class.java.name,
    R.xml.widget_coupled_preferences,
    2f
) {

    override fun generateWidget(
        width: Int,
        height: Int,
        widgetPreferences: SharedPreferences
    ): View {
        val view = layoutInflater.inflate(R.layout.widget_basic, null)

        return view
    }

}