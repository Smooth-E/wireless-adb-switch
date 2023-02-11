package com.smoothie.wirelessDebuggingSwitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager.EXTRA_WIFI_STATE
import android.net.wifi.WifiManager.EXTRA_PREVIOUS_WIFI_STATE
import android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN
import android.util.Log

class WifiReceiver : BroadcastReceiver() {

    companion object {

        interface OnWifiStateChangeListener {
            fun onWifiStateChanged(currentState: Int, previousState: Int)
        }

        private const val TAG = "WifiReceiver"

        private var LISTENERS = HashSet<OnWifiStateChangeListener>()

        var CURRENT_WIFI_STATE: Int = WIFI_STATE_UNKNOWN
            private set

        var PREVIOUS_WIFI_STATE: Int = WIFI_STATE_UNKNOWN
            private set

        fun addOnWifiStateChangeListener(listener: OnWifiStateChangeListener) =
            LISTENERS.add(listener)

        fun removeOnWifiStateChangeListener(listener: OnWifiStateChangeListener) =
            LISTENERS.remove(listener)

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.extras ?: return

        CURRENT_WIFI_STATE = extras.getInt(EXTRA_WIFI_STATE)
        PREVIOUS_WIFI_STATE = extras.getInt(EXTRA_PREVIOUS_WIFI_STATE)

        Log.d(TAG, "onReceive()")

        for (listener in LISTENERS)
            listener.onWifiStateChanged(CURRENT_WIFI_STATE, PREVIOUS_WIFI_STATE)
    }

}
