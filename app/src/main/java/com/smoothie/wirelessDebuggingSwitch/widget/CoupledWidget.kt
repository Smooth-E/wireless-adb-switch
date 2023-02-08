package com.smoothie.wirelessDebuggingSwitch.widget

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.smoothie.wirelessDebuggingSwitch.R

class CoupledWidget : BasicWidget(CoupledWidget::class.java.name) {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!InformationWidget.resolvePossibleCopyIntent(context, intent))
            super.onReceive(context, intent)
    }

    override fun generateRemoteViews(
        context: Context,
        widgetId: Int,
        preferences: SharedPreferences
    ): RemoteViews {
        val coreViews = RemoteViews(context.packageName, R.layout.widget_coupled_holder)

        coreViews.removeAllViews(R.id.holder_start)
        coreViews.removeAllViews(R.id.holder_end)

        val switchViews = super.generateRemoteViews(context, widgetId, preferences)
        val informationVies = InformationWidget.generateRemoteViews(context, widgetId, preferences)

        coreViews.addView(R.id.holder_start, switchViews)
        coreViews.addView(R.id.holder_end, informationVies)

        return coreViews
    }

}