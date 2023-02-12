package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.topjohnwu.superuser.Shell

object WirelessDebugging {

    private const val TAG = "Wireless Debugging"

    var enabled: Boolean
        get() {
            val command = "settings get global adb_wifi_enabled"
            val result = Shell.cmd(command).exec()
            return result.isSuccess && result.out.joinToString().trim().toInt() == 1
        }
        set(value) {
            val command = "settings put global adb_wifi_enabled ${if (value) 1 else 0}"
            Shell.cmd(command).exec()
        }

    fun getPort(): String {
        val command = "getprop service.adb.tls.port"
        val result = Shell.cmd(command).exec()

        if (!result.isSuccess)
            throw Exception("Failed to obtain wireless debugging port!")

        return result.out.joinToString()
    }

    fun getAddress(context: Context): String {
        val wm =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectionInfo = wm.connectionInfo
        val ipAddress = connectionInfo.ipAddress
        return Formatter.formatIpAddress(ipAddress)
    }

    fun getConnectionData(context: Context): String =
        "${getAddress(context)}:${getPort()}"

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

        val result = KdeConnect.sendClipboard(context, connectionData)

        if (!result.isSuccess) {
            val message = context.getString(R.string.message_failed_sending_clipboard)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            Log.w(TAG, result.toString())
            return
        }
    }

}
