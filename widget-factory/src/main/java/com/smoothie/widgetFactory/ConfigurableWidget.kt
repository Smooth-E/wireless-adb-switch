package com.smoothie.widgetFactory

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
import com.smoothie.widgetFactory.configuration.WidgetPreferences

abstract class ConfigurableWidget(private val className: String) : AppWidgetProvider() {

    companion object {

        private const val TAG = "ConfigurableWidget"

        private val widgetClassNames = HashSet<String>()

        fun addWidget(name: String) = widgetClassNames.add(name)

        fun updateAllWidgets(context: Context) {
            Log.i(TAG, "updateAllWidgets()")

            for (className in widgetClassNames) {
                val applicationContext = context.applicationContext
                val componentName = ComponentName(applicationContext, className)
                val manager = AppWidgetManager.getInstance(applicationContext)
                val widgetIds = manager.getAppWidgetIds(componentName)

                val intent = Intent()
                intent.component = componentName
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)

                context.sendBroadcast(intent)
            }
        }

    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        if (context != null)
            updateWidgets(context, intArrayOf(appWidgetId))
        else
            Log.d(TAG, "Widget options changed with null context!")
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        Log.d(TAG, "onUpdate()")

        if (appWidgetIds == null || context == null || appWidgetManager == null)
            return

        updateWidgets(context, appWidgetIds)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        if (context == null || appWidgetIds == null)
            return

        val applicationContext = context.applicationContext
        appWidgetIds.forEach { id ->
            val preferencesName = WidgetPreferences.getWidgetSharedPreferencesName(id)
            applicationContext.deleteSharedPreferences(preferencesName)
            Log.d(TAG, "Deleted widget with id $id")
        }
    }

    protected abstract fun generateRemoteViews(
        context: Context,
        widgetId: Int,
        preferences: SharedPreferences
    ): RemoteViews

    protected fun getPendingUpdateIntent(context: Context, updateIntent: Intent): PendingIntent {
        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, 0, updateIntent, flag)
    }

    private fun updateWidgets(context: Context, widgetIds: IntArray) {
        val manager = AppWidgetManager.getInstance(context)
        val applicationContext = context.applicationContext

        widgetIds.forEach { id ->
            val preferenceName = WidgetPreferences.getWidgetSharedPreferencesName(id)
            val preferences =
                applicationContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            manager.updateAppWidget(id, generateRemoteViews(context, id, preferences))
        }
    }

}
