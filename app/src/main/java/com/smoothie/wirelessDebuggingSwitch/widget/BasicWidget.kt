package com.smoothie.wirelessDebuggingSwitch.widget

import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.smoothie.wirelessDebuggingSwitch.PreferenceUtilities
import com.smoothie.wirelessDebuggingSwitch.R

class BasicWidget : SwitchWidget(BasicWidget::class.java.name) {

    override fun generateRemoteViews(
        context: Context,
        widgetId: Int,
        preferences: SharedPreferences
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_basic)

        val pendingIntent = getPendingUpdateIntent(context, createStateSwitchIntent(context))
        remoteViews.setOnClickPendingIntent(R.id.clickable, pendingIntent)

        val iconResource =
            if (switchState == SwitchState.Enabled)
                R.drawable.ic_round_wifi_24
            else
                R.drawable.ic_round_wifi_off_24
        remoteViews.setImageViewResource(R.id.image_view_status, iconResource)

        var textColor = PreferenceUtilities.getLightOrDarkTextColor(context, preferences)
        remoteViews.setTextColor(R.id.text_view_name, textColor)

        val text = when(switchState) {
            SwitchState.Enabled -> context.getString(R.string.state_enabled)
            SwitchState.Waiting -> context.getString(R.string.state_waiting)
            SwitchState.Disabled -> context.getString(R.string.state_disabled)
        }
        remoteViews.setTextViewText(R.id.text_view_status, text)

        val background: Int
        if (switchState == SwitchState.Enabled) {
            background = R.drawable.status_enabled
            textColor = R.color.colorSurface
        }
        else {
            background = R.drawable.status_disabled
            textColor = R.color.colorPrimary
        }

        val textColorValue = context.getColor(textColor)
        remoteViews.setInt(R.id.text_view_status, "setBackgroundResource", background)
        remoteViews.setInt(R.id.text_view_status, "setTextColor", textColorValue)

        RoundedWidgetUtilities.applyRemoteViewsParameters(context, preferences, remoteViews)

        return remoteViews
    }

}
