package com.smoothie.wirelessDebuggingSwitch.receiver

import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.smoothie.wirelessDebuggingSwitch.R

class BasicSwitchWidget : SwitchWidget() {

    override fun generateRemoteViews(
        context: Context,
        preferences: SharedPreferences,
        state: SwitchState
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_switch_small)
        remoteViews.setOnClickPendingIntent(R.id.clickable, getPendingUpdateIntent(context))

        val iconResource =
            if (state == SwitchState.Enabled)
                R.drawable.ic_round_wifi_24
            else
                R.drawable.ic_round_wifi_off_24
        remoteViews.setImageViewResource(R.id.image_view_status, iconResource)

        val text = when(state) {
            SwitchState.Enabled -> "Enabled"
            SwitchState.Waiting -> ""
            SwitchState.Disabled -> "Disabled"
        }
        remoteViews.setTextViewText(R.id.text_view_status, text)

        val background: Int
        val textColor: Int
        if (state == SwitchState.Enabled) {
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

        return remoteViews
    }

}
