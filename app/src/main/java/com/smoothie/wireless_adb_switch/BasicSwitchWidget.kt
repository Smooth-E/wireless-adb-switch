package com.smoothie.wireless_adb_switch

import android.content.Context
import android.widget.RemoteViews

class BasicSwitchWidget : AbstractSwitchWidget() {


    override fun getWidget(): AbstractSwitchWidget = this

    override fun generateRemoteViews(context: Context, status: SwitchState): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_base)

        remoteViews.setOnClickPendingIntent(R.id.clickable, getPendingUpdateIntent(context))

        val text = when(status) {
            SwitchState.Enabled -> "Enabled"
            SwitchState.Disabled -> "Disabled"
            SwitchState.Waiting -> "Waiting"
        }
        remoteViews.setTextViewText(R.id.text_view_status, text)

        return remoteViews
    }

}
