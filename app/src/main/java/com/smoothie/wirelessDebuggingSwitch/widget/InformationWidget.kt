package com.smoothie.wirelessDebuggingSwitch.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS
import android.content.*
import android.util.Log
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import android.widget.Toast
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.widgetFactory.WidgetUpdater
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.WirelessDebugging
import com.smoothie.wirelessDebuggingSwitch.getLightOrDarkTextColor
import com.smoothie.wirelessDebuggingSwitch.hasSufficientPrivileges

class InformationWidget : ConfigurableWidget(InformationWidget::class.java.name) {

    companion object {
        private const val TAG = "InformationWidget"
        private const val EXTRA_FLAG_COPY = "COPY_CONNECTION_INFORMATION"
        private const val REQUEST_CODE_COPY = 0
        private const val EXTRA_FLAG_UPDATE = "UPDATE_CONNECTION_INFORMATION"
        private const val REQUEST_CODE_UPDATE = 1
        private const val EXTRA_ADDRESS = "ADDRESS"
        private const val EXTRA_PORT = "PORT"
        private const val STATUS_ERROR = "ERROR"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val extraCopy = intent?.getBooleanExtra(EXTRA_FLAG_COPY, false)
        val extraUpdate = intent?.getBooleanExtra(EXTRA_FLAG_UPDATE, false)
        Log.d(TAG, "onReceive(): EXTRA_COPY: $extraCopy, EXTRA_UPDATE: $extraUpdate")

        if (resolvePossibleCopyIntent(context, intent))
            return

        if (resolvePossibleUpdateIntent(context, intent))
            return

        super.onReceive(context, intent)
    }

    override fun generateRemoteViews(
        context: Context,
        widgetId: Int,
        preferences: SharedPreferences
    ): RemoteViews {
        if (!hasSufficientPrivileges())
            return getMissingPrivilegesRemoteViews(context, preferences)

        val views = RemoteViews(context.packageName, R.layout.widget_information)
        applyRemoteViewsParameters(context, preferences, views)

        val debuggingEnabled = WirelessDebugging.getEnabled(context)
        views.setViewVisibility(R.id.data_enabled, if (debuggingEnabled) VISIBLE else GONE)
        views.setViewVisibility(R.id.data_disabled, if (!debuggingEnabled) VISIBLE else GONE)

        if (!debuggingEnabled) {
            val visibility = if (WidgetUpdater.enabled) GONE else VISIBLE
            views.setViewVisibility(R.id.button_update_disabled, visibility)
            val pendingIntent = createUpdatePendingIntent(context, widgetId)
            views.setOnClickPendingIntent(R.id.data_disabled, pendingIntent)
            return views
        }

        var connectionDataError = false
        var address: String
        var port: String
        try {
            address = WirelessDebugging.getAddress(context)
            port = WirelessDebugging.getPort(context)
        }
        catch (exception: Exception) {
            connectionDataError = true
            Log.e(TAG, "Failed to get connection data!")
            exception.printStackTrace()

            val stringError = context.getString(R.string.label_error)
            address = stringError
            port = stringError
        }

        views.setTextViewText(R.id.text_view_address, address)
        views.setTextViewText(R.id.text_view_port, port)

        val textColor = getLightOrDarkTextColor(context, preferences)
        views.setTextColor(R.id.text_view_status, textColor)
        views.setTextColor(R.id.text_view_name, textColor)

        val copyIntent = createReceivableIntent(context, widgetId)
        copyIntent.putExtra(EXTRA_FLAG_COPY, true)
        copyIntent.putExtra(EXTRA_ADDRESS, if (connectionDataError) STATUS_ERROR else address)
        copyIntent.putExtra(EXTRA_PORT, if (connectionDataError) STATUS_ERROR else port)

        val intentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val copyPendingIntent =
            PendingIntent.getBroadcast(context, REQUEST_CODE_COPY, copyIntent, intentFlags)
        views.setOnClickPendingIntent(R.id.data_enabled, copyPendingIntent)

        if (WidgetUpdater.enabled) {
            views.setViewVisibility(R.id.text_view_name, VISIBLE)
            views.setViewVisibility(R.id.text_view_short_name, GONE)
            views.setViewVisibility(R.id.button_update, GONE)
            return views
        }

        views.setViewVisibility(R.id.text_view_name, INVISIBLE)
        views.setViewVisibility(R.id.text_view_short_name, VISIBLE)
        views.setViewVisibility(R.id.button_update, VISIBLE)

        val updatePendingIntent = createUpdatePendingIntent(context, widgetId)
        views.setOnClickPendingIntent(R.id.button_update, updatePendingIntent)

        return views
    }

    private fun createUpdatePendingIntent(context: Context, widgetId: Int): PendingIntent {
        val intent = createReceivableIntent(context, widgetId)
        intent.putExtra(EXTRA_FLAG_UPDATE, true)
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, REQUEST_CODE_UPDATE, intent, flags)
    }

    private fun createReceivableIntent(context: Context, widgetId: Int): Intent {
        val intent = Intent(ACTION_APPWIDGET_UPDATE)
        intent.component = ComponentName(context, this::class.java.name)
        intent.putExtra(EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        return intent
    }

    private fun resolvePossibleUpdateIntent(context: Context?, intent: Intent?): Boolean {
        val extraPresent = intent?.getBooleanExtra(EXTRA_FLAG_UPDATE, false)
        if (context != null && extraPresent == true) {
            Log.d(TAG, "Force update intent caught!")
            WidgetUpdater.forceUpdate(context)
            return true
        }

        return false
    }

    private fun resolvePossibleCopyIntent(context: Context?, intent: Intent?): Boolean {
        val extras = intent?.extras
        val extraPresent = intent?.getBooleanExtra(EXTRA_FLAG_COPY, false) == true
        if (context == null || extras == null || !extraPresent)
            return false

        val address = extras.getString(EXTRA_ADDRESS)
        val port = extras.getString(EXTRA_PORT)

        if (address == STATUS_ERROR || port == STATUS_ERROR) {
            val message = context.getString(R.string.message_error_copying_connection_data)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            return false
        }

        val label = "Connection Data"
        val content = "$address:$port"
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, content))

        val message = context.getString(R.string.message_copied)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        return true
    }

}
