package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log

object WirelessDebugging : SuperShell() {

    private const val TAG = "Wireless Debugging"

    var enabled: Boolean = false
        get() {
            return try {
                val command = "settings get global adb_wifi_enabled"
                Integer.parseInt(execute(command)) == 1
            } catch (exception: RootEvaluationException) {
                Log.e(TAG, "Failed to get ADB status!")
                false
            }
        }
        set(value) {
            field = try {
                execute("settings put global adb_wifi_enabled ${if (value) 1 else 0}")
                value
            }
            catch (exception: RootEvaluationException) {
                Log.e(TAG, "Failed to set ADB status!")
                false
            }
        }

    fun getPort(): String = execute("getprop service.adb.tls.port")

    fun getAddress(context: Context): String {
        val wm =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectionInfo = wm.connectionInfo
        val ipAddress = connectionInfo.ipAddress
        return Formatter.formatIpAddress(ipAddress)
    }

}
