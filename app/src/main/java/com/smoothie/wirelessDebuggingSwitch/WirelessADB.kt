package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter

class WirelessADB : SuperShell() {

    companion object {

        var enabled: Boolean = false
            get() = Integer.parseInt(execute("settings get global adb_wifi_enabled")) == 1
            set(value) {
                execute("settings put global adb_wifi_enabled ${if (value) 1 else 0}")
                field = value
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

}
