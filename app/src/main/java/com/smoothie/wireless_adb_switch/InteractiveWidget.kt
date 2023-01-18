package com.smoothie.wireless_adb_switch

import android.app.PendingIntent.*
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews

class InteractiveWidget(private val adb: WirelessADB = WirelessADB()) : AppWidgetProvider() {

    companion object {
        private val INTENT_EXTRA_NAME = "WIDGET_UPDATE_INTENT"
    }

    private enum class SwitchState {
        Disabled,
        Waiting,
        Enabled
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) = updateWidget(context, appWidgetManager, appWidgetId)

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        if (appWidgetIds == null || context == null || appWidgetManager == null)
            return

        for (widgetId in appWidgetIds)
            updateWidget(context, appWidgetManager, widgetId)
    }

     override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null && intent.getBooleanExtra(INTENT_EXTRA_NAME, false)) {
            Log.d("", "Button clicked!")

            Thread {
                val status = !adb.enabled
                adb.enabled = status

                val manager = AppWidgetManager.getInstance(context)
                val componentName =
                    ComponentName(context.applicationContext, InteractiveWidget::class.java)
                val ids = manager.getAppWidgetIds(componentName)
                manager.updateAppWidget(ids, generateRemoteViews(context, if (status) SwitchState.Enabled else SwitchState.Disabled))
            }.start()

            val manager = AppWidgetManager.getInstance(context)
            val componentName =
                ComponentName(context.applicationContext, InteractiveWidget::class.java)
            val ids = manager.getAppWidgetIds(componentName)
            manager.updateAppWidget(ids, generateRemoteViews(context, SwitchState.Waiting))
        }

         super.onReceive(context, intent)
    }

    private fun generateRemoteViews(context: Context, status: SwitchState): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_base)

        val manager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context.applicationContext, InteractiveWidget::class.java)
        val ids = manager.getAppWidgetIds(componentName)

        val intent = Intent(context, InteractiveWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        intent.putExtra(INTENT_EXTRA_NAME, true)

        val pendingIntent =
            getBroadcast(context, 0, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.clickable, pendingIntent)

        val text = when(status) {
            SwitchState.Enabled -> "Enabled"
            SwitchState.Disabled -> "Disabled"
            SwitchState.Waiting -> "Waiting"
        }

        remoteViews.setTextViewText(R.id.text_view_status, text)

        return remoteViews
    }

    private fun updateWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int
    ) {
        if (context == null || appWidgetManager == null)
            return

        val status = adb.enabled
        appWidgetManager.updateAppWidget(appWidgetId, generateRemoteViews(context, if (adb.enabled) SwitchState.Enabled else SwitchState.Disabled ))
    }

}
