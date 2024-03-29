package com.smoothie.wirelessDebuggingSwitch.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS
import android.content.*
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import android.widget.Toast
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.WirelessDebugging
import com.smoothie.wirelessDebuggingSwitch.getLightOrDarkTextColor
import com.smoothie.wirelessDebuggingSwitch.hasSufficientPrivileges

class InformationWidget : ConfigurableWidget(InformationWidget::class.java.name) {

    companion object {
        private const val TAG = "InformationWidget"
        private const val EXTRA_FLAG = "COPY_CONNECTION_INFORMATION"
        private const val EXTRA_ADDRESS = "ADDRESS"
        private const val EXTRA_PORT = "PORT"
        private const val STATUS_ERROR = "ERROR"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!resolvePossibleCopyIntent(context, intent))
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

        if (!debuggingEnabled)
            return views

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

        val intent = Intent(ACTION_APPWIDGET_UPDATE)
        intent.component = ComponentName(context, this::class.java.name)
        intent.putExtra(EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        intent.putExtra(EXTRA_FLAG, true)
        intent.putExtra(EXTRA_ADDRESS, if (connectionDataError) STATUS_ERROR else address)
        intent.putExtra(EXTRA_PORT, if (connectionDataError) STATUS_ERROR else port)

        val intentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, intentFlags)
        views.setOnClickPendingIntent(R.id.data_enabled, pendingIntent)

        return views
    }

    private fun resolvePossibleCopyIntent(context: Context?, intent: Intent?): Boolean {
        val extras = intent?.extras

        if (intent?.extras?.getBoolean(EXTRA_FLAG) == true)
            Log.d(TAG, "Extra is there")

        if (context == null || extras == null || !extras.getBoolean(EXTRA_FLAG)) {
            return false
        }

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
