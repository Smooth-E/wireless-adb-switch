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

abstract class ConfigurableWidget(name: String) : AppWidgetProvider() {

    companion object {

        private const val TAG = "ConfigurableWidget"

        private val widgetClassNames = HashSet<String>()

        fun addWidget(name: String) = widgetClassNames.add(name)

        fun getAllWidgetIds(context: Context): IntArray {
            val manager = AppWidgetManager.getInstance(context)
            val ids = ArrayList<Int>()
            widgetClassNames.forEach {
                val componentName = ComponentName(context.applicationContext, it)
                ids.addAll(manager.getAppWidgetIds(componentName).toList())
            }
            return ids.toIntArray()
        }

        @JvmStatic
        fun createBasicIntent(context: Context): Intent {
            val intent = Intent()
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getAllWidgetIds(context))
            return intent
        }

    }

    init {
        addWidget(name)
        Log.d(TAG, "Added a name: $name")
        Log.d(TAG, "All names added: $widgetClassNames")
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

    protected fun updateAllWidgets(context: Context) =
        updateWidgets(context, getAllWidgetIds(context))

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
