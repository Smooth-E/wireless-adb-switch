package com.smoothie.wirelessDebuggingSwitch.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.wirelessDebuggingSwitch.PreferenceUtilities
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.WirelessDebugging

class InformationWidget : ConfigurableWidget(InformationWidget::class.java.name) {

    override fun generateRemoteViews(
        context: Context,
        widgetId: Int,
        preferences: SharedPreferences
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_information)

        RoundedWidgetUtilities.applyRemoteViewsParameters(context, preferences, views)

        val debuggingEnabled = WirelessDebugging.enabled
        views.setViewVisibility(R.id.data_enabled, if (debuggingEnabled) VISIBLE else GONE)
        views.setViewVisibility(R.id.data_disabled, if (!debuggingEnabled) VISIBLE else GONE)

        if (!debuggingEnabled)
            return views

        var address: String
        var port: String
        try {
            address = WirelessDebugging.getAddress(context)
            port = WirelessDebugging.getPort()
        }
        catch (exception: Exception) {
            Log.e("Information Widget", "Failed to get connection data!")
            exception.printStackTrace()

            val stringError = context.getString(R.string.label_error)
            address = stringError
            port = stringError
        }

        views.setTextViewText(R.id.text_view_address, address)
        views.setTextViewText(R.id.text_view_port, port)

        val textColor = PreferenceUtilities.getLightOrDarkTextColor(context, preferences)
        views.setTextColor(R.id.text_view_status, textColor)
        views.setTextColor(R.id.text_view_name, textColor)

        return views
    }

}
