package com.smoothie.wirelessDebuggingSwitch

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup

class BasicWidgetConfigurationActivity :
    WidgetConfigurationActivity(R.xml.preferences_widget_basic) {

    override fun generateWidget(preferences: SharedPreferences): View {
        val view = layoutInflater.inflate(R.layout.widget_switch_small, null)

        val background = view.findViewById<ViewGroup>(R.id.clickable)

        val useColorfulBackground =
            preferences.getBoolean(getString(R.string.key_use_colorful_background), false)
        val transparency =
            preferences.getInt(getString(R.string.key_background_transparency), 100) / 100f

        val colorId =
            if (useColorfulBackground)
                R.color.colorPrimaryContainer
            else
                R.color.colorSurface

        val color = Color.valueOf(theme.resources.getColor(colorId, theme))
        val colorInt = Color.argb(transparency, color.red(), color.green(), color.blue())

        background.background = ColorDrawable(colorInt)

        return view
    }


}