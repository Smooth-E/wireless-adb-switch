package com.smoothie.wireless_adb_switch

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews

class InteractiveWidget : AppWidgetProvider() {

    companion object {
        private val EXTRA_NAME: String = "INTENT_TO_CHANGE_ADB_STATUS"
        private val EXTRA_CHANGE_ADB_STATUS = 0
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) = updateWidget(context, appWidgetManager, appWidgetId, WirelessADB())

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        val adb = WirelessADB()
        for (widgetId in appWidgetIds!!)
            updateWidget(context, appWidgetManager, widgetId, adb)
    }

    private fun updateWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        adb: WirelessADB
    ) {
        Log.d("TAG", "Updating widget!")

        val remoteViews = RemoteViews(context!!.packageName, R.layout.widget_base)

        val intent = Intent(context, InteractiveWidget::class.java)
        intent.putExtra(EXTRA_NAME, EXTRA_CHANGE_ADB_STATUS)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, FLAG_IMMUTABLE)

        remoteViews.setOnClickPendingIntent(R.id.clickable, pendingIntent)

        remoteViews
            .setTextViewText(R.id.text_view_status, if (adb.enabled) "Enabled" else "Disabled")

        appWidgetManager!!.updateAppWidget(appWidgetId, remoteViews)
    }

}
