package com.smoothie.wirelessDebuggingSwitch.widget.basic

import android.content.SharedPreferences
import android.view.View
import android.widget.ImageView
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.widgetFactory.WidgetConfigurationActivity
import com.smoothie.wirelessDebuggingSwitch.Utilities

class ConfigurationActivity :
    WidgetConfigurationActivity(R.xml.preferences_widget_basic, 1f / 1f) {

    override fun generateWidget(width: Int, height: Int, preferences: SharedPreferences): View {
        val view = layoutInflater.inflate(R.layout.widget_switch_small, null)

        val radius =  Utilities.getWidgetCornerRadius(this, preferences)

        val cornerBitmap = Utilities.generateWidgetCornerBitmap(this, preferences, radius)
        view.findViewById<ImageView>(R.id.corner_top_left).setImageBitmap(cornerBitmap)
        view.findViewById<ImageView>(R.id.corner_top_right).setImageBitmap(cornerBitmap)
        view.findViewById<ImageView>(R.id.corner_bottom_left).setImageBitmap(cornerBitmap)
        view.findViewById<ImageView>(R.id.corner_bottom_right).setImageBitmap(cornerBitmap)

        val centerBitmap = Utilities.generateRectangleBitmapForWidget(this, preferences)
        view.findViewById<ImageView>(R.id.side_left).setImageBitmap(centerBitmap)
        view.findViewById<ImageView>(R.id.side_right).setImageBitmap(centerBitmap)
        view.findViewById<ImageView>(R.id.side_top).setImageBitmap(centerBitmap)
        view.findViewById<ImageView>(R.id.side_bottom).setImageBitmap(centerBitmap)
        view.findViewById<ImageView>(R.id.center).setImageBitmap(centerBitmap)

        return view
    }

}
