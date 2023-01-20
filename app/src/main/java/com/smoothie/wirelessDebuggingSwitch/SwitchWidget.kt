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

abstract class SwitchWidget : AppWidgetProvider() {

    protected enum class SwitchState {
        Disabled,
        Waiting,
        Enabled
    }

    companion object {

        const val INTENT_FLAG_SWITCH_STATE =
            "com.smoothie.wirelessDebuggingSwitch.intent.FLAG_SWITCH_STATE"
        const val INTENT_FLAG_UPDATE =
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

        private fun createBasicIntent(context: Context): Intent {
            val intent = Intent()
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getAllWidgetIds(context))
            return intent
        }

        private fun createStateSwitchIntent(context: Context): Intent {
            val intent = createBasicIntent(context)
            intent.putExtra(INTENT_FLAG_SWITCH_STATE, true)
            return intent
        }

        fun createUpdateIntent(context: Context): Intent {
            val intent = createBasicIntent(context)
            intent.putExtra(INTENT_FLAG_UPDATE, true)
            return intent
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
        if (intent?.hasExtra(INTENT_FLAG_SWITCH_STATE) == true) {
            updateAllWidgets(context!!, SwitchState.Waiting)

            Thread {
                val status = !WirelessADB.enabled
                updateAllWidgets(context, if (status) SwitchState.Enabled else SwitchState.Disabled)
                WirelessADB.enabled = status
            }.start()
        }
        else if (intent?.hasExtra(INTENT_FLAG_UPDATE) == true) {
            Thread {
                val status = if (WirelessADB.enabled) SwitchState.Enabled else SwitchState.Disabled
                updateAllWidgets(context!!, status)
            }.start()
        }
        else {
            super.onReceive(context, intent)
            return
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        if (context != null)
            WidgetUpdater.enable(context)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        if (context != null)
            WidgetUpdater.disable(context)
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
        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, 0, createStateSwitchIntent(context), flag)
    }

}
