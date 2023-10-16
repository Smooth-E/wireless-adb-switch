package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager

object WirelessDebugging {

    private const val TAG = "WirelessDebuggingFeature"

    fun getEnabled(context: Context): Boolean {
        val command = "settings get --user current global adb_wifi_enabled"
        val result = executeShellCommand(context, command)
        val value =
            result != null &&
            result.isSuccess &&
            result.out.joinToString("\n").trim().toInt() == 1
        Log.d(TAG, "getEnabled($context) returned $value")
        return value
    }

    fun setEnabled(context: Context, value: Boolean) {
        val state = if (value) 1 else 0
        val command = "settings put --user current global adb_wifi_enabled $state"
        executeShellCommand(context, command)
    }

    fun getPort(context: Context): String {
        val command = "getprop service.adb.tls.port"
        val result = executeShellCommand(context, command)

        if (result == null || !result.isSuccess)
            throw Exception("Failed to obtain wireless debugging port!")

        return result.out.joinToString("\n")
    }

    fun getAddress(context: Context): String {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectionInfo = wifiManager.connectionInfo
        val ipAddress = connectionInfo.ipAddress
        return Formatter.formatIpAddress(ipAddress)
    }

    fun getConnectionData(context: Context): String =
        "${getAddress(context)}:${getPort(context)}"

    /** Place connection data into the clipboard. Does not check whether the debugging is enabled */
    fun copyConnectionData(context: Context) =
        copyText(context, "Wireless debugging connection data", getConnectionData(context))

    /**
     * Synchronize connection data if device is connected via Wireless ADB and
     * synchronization is enabled in app's settings.
     *
     * @param context context used to create a WIFI_SERVICE
     */
    fun syncConnectionData(context: Context) {
        val connectionInfo: String
        try {
            connectionInfo = getConnectionData(context)
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
                KdeConnect.CLIPBOARD_PREFIX + connectionInfo
            else
                connectionInfo

        copyText(context, "Data for KDE Connect", connectionData)
        val result = KdeConnect.sendClipboard(context)
            ?: return

        if (!result.isSuccess) {
            val message = context.getString(R.string.message_failed_sending_clipboard)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            Log.w(TAG, result.toString())
        }
    }

}
