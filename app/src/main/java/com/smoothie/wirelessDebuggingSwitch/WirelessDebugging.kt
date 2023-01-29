package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
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

}
