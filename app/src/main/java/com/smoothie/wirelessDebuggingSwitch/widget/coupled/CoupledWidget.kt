package com.smoothie.wirelessDebuggingSwitch.widget.coupled

import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.widget.RoundedWidgetUtilities
import com.smoothie.wirelessDebuggingSwitch.widget.SwitchWidget

class CoupledWidget : SwitchWidget(CoupledWidget::class.java.name) {

    override fun generateRemoteViews(
        context: Context,
        widgetId: Int,
        preferences: SharedPreferences
    ): RemoteViews {
        val packageName = context.packageName

        val coreViews = RemoteViews(packageName, R.layout.widget_coupled_holder)
        val switchViews = RemoteViews(packageName, R.layout.widget_basic)
        val infoViews = RemoteViews(packageName, R.layout.widget_coupled_info)

        RoundedWidgetUtilities.applyRemoteViewsParameters(context, preferences, switchViews)
        RoundedWidgetUtilities.applyRemoteViewsParameters(context, preferences, infoViews)

        coreViews.addView(R.id.holder_start, switchViews)
        coreViews.addView(R.id.holder_end, infoViews)

        return coreViews
    }

}
