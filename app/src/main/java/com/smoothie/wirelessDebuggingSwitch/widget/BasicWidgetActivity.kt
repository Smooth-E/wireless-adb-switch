package com.smoothie.wirelessDebuggingSwitch.widget

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.View
import android.widget.TextView
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.PreferenceUtilities
import com.smoothie.wirelessDebuggingSwitch.PrivilegeCheckingWidgetConfigurationActivity

class BasicWidgetActivity : PrivilegeCheckingWidgetConfigurationActivity(
    R.xml.widget_preferences,
    1f
) {

    @SuppressLint("InflateParams")
    override fun generateWidget(
        width: Int,
        height: Int,
        widgetPreferences: SharedPreferences
    ): View {
        val view = layoutInflater.inflate(R.layout.widget_basic, null)

        RoundedWidgetUtilities.applyPreviewParameters(this, widgetPreferences, view)

        val textColor = PreferenceUtilities.getLightOrDarkTextColor(this, widgetPreferences)
        view.findViewById<TextView>(R.id.text_view_name).setTextColor(textColor)

        return view
    }

}
