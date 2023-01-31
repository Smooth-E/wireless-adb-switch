package com.smoothie.wirelessDebuggingSwitch.widget

import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.wirelessDebuggingSwitch.KdeConnect
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.WirelessDebugging
import com.topjohnwu.superuser.Shell

abstract class SwitchWidget(name: String) : ConfigurableWidget(name) {

    enum class SwitchState {
        Disabled,
        Waiting,
        Enabled
    }

    companion object {

        private const val TAG = "SwitchWidget"
        private const val CLIPBOARD_PREFIX = "connect-wireless-debugging://"

        const val INTENT_FLAG_CHANGE_STATE =
            "com.smoothie.wirelessDebuggingSwitch.intent.FLAG_CHANGE_STATE"

        @JvmStatic
        protected var switchState: SwitchState = SwitchState.Disabled
            private set

        @JvmStatic
        protected fun createStateSwitchIntent(context: Context): Intent {
            val intent = createBasicIntent(context)
            intent.putExtra(INTENT_FLAG_CHANGE_STATE, true)
            return intent
        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.hasExtra(INTENT_FLAG_CHANGE_STATE) != true) {
            super.onReceive(context, intent)
            return
        }

        switchState = SwitchState.Waiting
        updateAllWidgets(context!!)

        WirelessDebugging.enabled = !WirelessDebugging.enabled
        switchState = if (WirelessDebugging.enabled)
            SwitchState.Enabled
        else
            SwitchState.Disabled

        updateAllWidgets(context)

        if (switchState != SwitchState.Enabled || !KdeConnect.isInstalled(context))
            return

        val connectionAddress: String
        try {
            val port = WirelessDebugging.getPort()
            val address = WirelessDebugging.getAddress(context)
            connectionAddress = "$address:$port"
        }
        catch (exception: Exception) {
            Log.e(TAG, "Unable to get connection address and port.")
            exception.printStackTrace()
            return
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        var preferenceKey = context.getString(R.string.key_enable_kde_connect)
        val kdeIntegrationEnabled = preferences.getBoolean(preferenceKey, true)

        if (!KdeConnect.isInstalled(context) || !kdeIntegrationEnabled)
            return

        preferenceKey = context.getString(R.string.key_prefix_connection_data)
        val prefixConnectionData = preferences.getBoolean(preferenceKey, true)

        val connectionData =
            if (prefixConnectionData)
                CLIPBOARD_PREFIX + connectionAddress
            else
                connectionAddress

        val result = KdeConnect.sendClipboard(context, connectionData)

        if (!result.isSuccess) {
            val message = context.getString(R.string.message_failed_sending_clipboard)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            Log.w(TAG, result.toString())
            return
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        switchState = if (WirelessDebugging.enabled) SwitchState.Enabled else SwitchState.Disabled
    }

}
