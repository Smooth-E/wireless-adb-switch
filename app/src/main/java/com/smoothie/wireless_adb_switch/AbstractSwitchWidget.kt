package com.smoothie.wireless_adb_switch

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews

abstract class AbstractSwitchWidget : AppWidgetProvider() {

    companion object {
        @JvmStatic
        protected val INTENT_EXTRA_NAME = "WIDGET_UPDATE_INTENT"
        @JvmStatic
        private val PRESENT_WIDGETS = HashSet<AbstractSwitchWidget>()
    }

    protected enum class SwitchState {
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
        PRESENT_WIDGETS.add(this)

        val noAction =
             context == null ||
             intent == null ||
             !intent.getBooleanExtra(INTENT_EXTRA_NAME, false)

        if (noAction) {
            super.onReceive(context, intent)
            return
        }

        updateAllWidgets(context!!, SwitchState.Waiting)

        Thread {
            val status = !WirelessADB.enabled
            updateAllWidgets(context, if (status) SwitchState.Enabled else SwitchState.Disabled)
            WirelessADB.enabled = status
        }.start()
    }

    protected abstract fun getWidget(): AbstractSwitchWidget

    private fun getAllWidgetIds(context: Context): IntArray {
        val manager = AppWidgetManager.getInstance(context)
        val ids = ArrayList<Int>()
        for (widget in PRESENT_WIDGETS) {
            val componentName =
                ComponentName(context.applicationContext, widget::class.java)
            ids.addAll(manager.getAppWidgetIds(componentName).toList())
        }
        return ids.toIntArray()
    }

    private fun updateAllWidgets(context: Context, status: SwitchState) {
        val manager = AppWidgetManager.getInstance(context)
        manager.updateAppWidget(getAllWidgetIds(context), generateRemoteViews(context, status))
    }

    protected abstract fun generateRemoteViews(context: Context, status: SwitchState): RemoteViews

    private fun updateWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int
    ) {
        if (context == null || appWidgetManager == null)
            return

        val status = if (WirelessADB.enabled) SwitchState.Enabled else SwitchState.Disabled
        appWidgetManager.updateAppWidget(appWidgetId, generateRemoteViews(context, status))
    }

    protected fun getPendingUpdateIntent(context: Context): PendingIntent {
        val intent = Intent()
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getAllWidgetIds(context))
        intent.putExtra(INTENT_EXTRA_NAME, true)

        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, 0, intent, flag)
    }

}
