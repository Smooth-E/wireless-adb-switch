package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.widget.RemoteViews

class BasicSwitchWidget : SwitchWidget() {

    override fun generateRemoteViews(context: Context, status: SwitchState): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_switch_small)

        remoteViews.setOnClickPendingIntent(R.id.clickable, getPendingUpdateIntent(context))

        val iconResource =
            if (status == SwitchState.Enabled)
                R.drawable.ic_round_wifi_24
            else
                R.drawable.ic_round_wifi_off_24
        remoteViews.setImageViewResource(R.id.image_view_status, iconResource)

        val text = when(status) {
            SwitchState.Enabled -> "Enabled"
            SwitchState.Waiting -> ""
            SwitchState.Disabled -> "Disabled"
        }
        remoteViews.setTextViewText(R.id.text_view_status, text)

        val background: Int
        val textColor: Int
        if (status == SwitchState.Enabled) {
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
