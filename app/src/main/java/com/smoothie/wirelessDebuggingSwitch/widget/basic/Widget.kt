package com.smoothie.wirelessDebuggingSwitch.widget.basic

import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.Utilities
import com.smoothie.wirelessDebuggingSwitch.widget.SwitchWidget

class Widget : SwitchWidget() {

    override fun generateRemoteViews(
        context: Context,
        widgetId: Int,
        preferences: SharedPreferences
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_switch_small)
        remoteViews.setOnClickPendingIntent(R.id.clickable, getPendingUpdateIntent(
            context,
            createStateSwitchIntent(context)
        ))


        val iconResource =
            if (switchState == SwitchState.Enabled)
                R.drawable.ic_round_wifi_24
            else
                R.drawable.ic_round_wifi_off_24
        remoteViews.setImageViewResource(R.id.image_view_status, iconResource)

        val text = when(switchState) {
            SwitchState.Enabled -> "Enabled"
            SwitchState.Waiting -> ""
            SwitchState.Disabled -> "Disabled"
        }
        remoteViews.setTextViewText(R.id.text_view_status, text)

        val background: Int
        val textColor: Int
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

        val radius = Utilities.getWidgetCornerRadius(context, preferences)

        val cornerBitmap = Utilities.generateWidgetCornerBitmap(context, preferences, radius)
        remoteViews.setBitmap(R.id.corner_bottom_left, "setImageBitmap", cornerBitmap)
        remoteViews.setBitmap(R.id.corner_bottom_right, "setImageBitmap", cornerBitmap)
        remoteViews.setBitmap(R.id.corner_top_left, "setImageBitmap", cornerBitmap)
        remoteViews.setBitmap(R.id.corner_top_right, "setImageBitmap", cornerBitmap)

        val centerBitmap = Utilities.generateRectangleBitmapForWidget(context, preferences)
        remoteViews.setBitmap(R.id.side_top, "setImageBitmap", centerBitmap)
        remoteViews.setBitmap(R.id.side_bottom, "setImageBitmap", centerBitmap)
        remoteViews.setBitmap(R.id.side_left, "setImageBitmap", centerBitmap)
        remoteViews.setBitmap(R.id.side_right, "setImageBitmap", centerBitmap)
        remoteViews.setBitmap(R.id.center, "setImageBitmap", centerBitmap)

        return remoteViews
    }

}
