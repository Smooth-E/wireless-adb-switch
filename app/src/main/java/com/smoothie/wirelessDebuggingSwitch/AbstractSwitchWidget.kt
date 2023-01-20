package com.smoothie.wirelessDebuggingSwitch

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews

abstract class AbstractSwitchWidget : AppWidgetProvider() {

    protected enum class SwitchState {
        Disabled,
        Waiting,
        Enabled
    }

    companion object {

        @JvmStatic
        val INTENT_EXTRA_FLAG_NAME =
            "com.smoothie.wirelessDebuggingSwitch.intent.FLAG_UPDATE_WIDGETS"

        private val WIDGET_CLASS_NAMES = arrayListOf<String>(
            BasicSwitchWidget::class.java.name
        )

        fun getAllWidgetIds(context: Context): IntArray {
            val manager = AppWidgetManager.getInstance(context)
            val ids = ArrayList<Int>()
            WIDGET_CLASS_NAMES.forEach {
                val componentName = ComponentName(context.applicationContext, it)
                ids.addAll(manager.getAppWidgetIds(componentName).toList())
            }
            return ids.toIntArray()
        }

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
        Log.d("onUpdate", "${context == null} ${appWidgetIds == null} ${appWidgetManager == null}")
        if (appWidgetIds == null || context == null || appWidgetManager == null)
            return

        appWidgetIds.forEach {
            updateWidget(context, appWidgetManager, it)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.hasExtra(INTENT_EXTRA_FLAG_NAME) == false) {
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
        val intent = Intent(context, BasicSwitchWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getAllWidgetIds(context))
        intent.putExtra(INTENT_EXTRA_FLAG_NAME, true)

        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, 0, intent, flag)
    }

}
