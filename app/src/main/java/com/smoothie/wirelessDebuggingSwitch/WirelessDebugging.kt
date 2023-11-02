package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.topjohnwu.superuser.Shell

object WirelessDebugging {

    private const val TAG = "WirelessDebuggingFeature"

    fun getEnabled(context: Context): Boolean {
        val command = "settings get global adb_wifi_enabled"
        val result = executeShellCommand(command, context)
        val value = result.isNotBlank() && result.toInt() == 1
        Log.d(TAG, "getEnabled($context) returned $value")
        return value
    }

    fun setEnabled(context: Context, value: Boolean) {
        val state = if (value) 1 else 0
        val command = "settings put --user current global adb_wifi_enabled $state"
        executeShellCommand(command, context)
    }

    fun getPort(context: Context): String =
        when (getPrivilegeLevel(PrivilegeLevel.Shizuku, context)) {
            PrivilegeLevel.Root ->
                Shell.cmd("getprop service.adb.tls.port").exec().out.toString()
            PrivilegeLevel.Shizuku ->
                ShizukuUtilities.getWirelessAdbPort()
            else -> ""
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
        if (!hasSufficientPrivileges(PrivilegeLevel.Root))
            return

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

        preferenceKey = context.getString(R.string.key_connection_data_prefix)
        val defaultPrefix = context.getString(R.string.default_connection_data_prefix)
        val connectionDataPrefix = preferences.getString(preferenceKey, defaultPrefix)

        val connectionData =
            if (prefixConnectionData)
                connectionDataPrefix + connectionInfo
            else
                connectionInfo

        copyText(context, "Data for KDE Connect", connectionData)
        val result = KdeConnect.sendClipboard()

        if (!result.isSuccess) {
            val message = context.getString(R.string.message_failed_sending_clipboard)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            Log.w(TAG, result.toString())
        }
    }

}
