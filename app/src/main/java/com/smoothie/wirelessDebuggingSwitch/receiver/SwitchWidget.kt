package com.smoothie.wirelessDebuggingSwitch.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.smoothie.wirelessDebuggingSwitch.WirelessADB

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
        const val SHARED_PREFERENCES_NAME_PREFIX = "widget_shared_preferences_"

        private const val TAG = "SwitchWidget"

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
    ) {
        if (context != null)
            updateWidgets(context, intArrayOf(appWidgetId), getSwitchState())
        else
            Log.d(TAG, "Widget options changed with null context!")
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        Log.d("onUpdate", "${context == null} ${appWidgetIds == null} ${appWidgetManager == null}")
        if (appWidgetIds == null || context == null || appWidgetManager == null)
            return

        updateWidgets(context, appWidgetIds, getSwitchState())
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.hasExtra(INTENT_FLAG_SWITCH_STATE) == true) {
            updateAllWidgets(context!!, SwitchState.Waiting)

            Thread {
                val state = !WirelessADB.enabled
                updateAllWidgets(context, if (state) SwitchState.Enabled else SwitchState.Disabled)
                WirelessADB.enabled = state
            }.start()
        }
        else if (intent?.hasExtra(INTENT_FLAG_UPDATE) == true) {
            Thread {
                updateAllWidgets(context!!, getSwitchState())
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

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        if (context == null || appWidgetIds == null)
            return

        val applicationContext = context.applicationContext
        appWidgetIds.forEach { id ->
            applicationContext.deleteSharedPreferences(getSharedPreferencesName(id))
            Log.d(TAG, "Deleted widget with id $id")
        }
    }

    private fun getSharedPreferencesName(widgetId: Int): String =
        "$SHARED_PREFERENCES_NAME_PREFIX$widgetId"

    private fun updateWidgets(context: Context, widgetIds: IntArray, state: SwitchState) {
        val manager = AppWidgetManager.getInstance(context)
        val applicationContext = context.applicationContext
        widgetIds.forEach { id ->
            val preferenceName = getSharedPreferencesName(id)
            val preferences =
                applicationContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            manager.updateAppWidget(id, generateRemoteViews(context, preferences, state))
        }
    }

    private fun updateAllWidgets(context: Context, state: SwitchState) =
        updateWidgets(context, getAllWidgetIds(context), state)

    protected abstract fun generateRemoteViews(
        context: Context,
        preferences: SharedPreferences,
        state: SwitchState
    ): RemoteViews

    protected fun getPendingUpdateIntent(context: Context): PendingIntent {
        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, 0, createStateSwitchIntent(context), flag)
    }

    private fun getSwitchState(): SwitchState =
        if (WirelessADB.enabled) SwitchState.Enabled else SwitchState.Disabled

}
