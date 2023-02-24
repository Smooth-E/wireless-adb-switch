package com.smoothie.wirelessDebuggingSwitch

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.net.wifi.WifiManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.content.ContextCompat
import com.smoothie.wirelessDebuggingSwitch.WifiReceiver.Companion.OnWifiStateChangeListener

class QSTileService : TileService() {

    companion object {
        private const val TAG = "QSTileService"
    }

    private val wifiStateChangeListener = object : OnWifiStateChangeListener {
        override fun onWifiStateChanged(currentState: Int, previousState: Int) = updateTile()
    }

    // Why @SuppressLint("WrongConstant")?
    // Android Studio reports ContextCompat.RECEIVER_NOT_EXPORTED as a wrong constant even tho it is
    // stated to use it in the documentation.
    // https://developer.android.com/guide/components/broadcasts#context-registered-receivers
    // 'Context.RECEIVER_NOT_EXPORTED' however is only accessible on API 33+
    @SuppressLint("WrongConstant")
    override fun onCreate() {
        super.onCreate()

        val receiver = WifiReceiver()
        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)

        val flags =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                ContextCompat.RECEIVER_NOT_EXPORTED
            else
                Context.RECEIVER_NOT_EXPORTED

        registerReceiver(receiver, intentFilter, flags)

        WifiReceiver.addOnWifiStateChangeListener(wifiStateChangeListener)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "Tile onClick() called!")

        WirelessDebugging.enabled = !WirelessDebugging.enabled
        updateTile()

        if (WirelessDebugging.enabled)
            WirelessDebugging.syncConnectionData(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        WifiReceiver.removeOnWifiStateChangeListener(wifiStateChangeListener)

        try {
            unregisterReceiver(WifiReceiver())
        }
        catch (expected: IllegalArgumentException) {
            Log.w(TAG, "Failed to unregister the WIFI receiver!")
        }
    }

    private fun updateTile() {
        Log.d(TAG, "Tile is being updated!")

        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.icon = Icon.createWithResource(this, R.drawable.baseline_wifi_off_24)
            qsTile.subtitle = getString(R.string.qs_tile_subtitle_unavailable)
            qsTile.updateTile()
            return
        }

        qsTile.icon = Icon.createWithResource(this, R.drawable.baseline_phonelink_24)

        if (WirelessDebugging.enabled) {
            var label: String
            var message: String

            try {
                label = getString(R.string.qs_tile_label_enabled)
                message = WirelessDebugging.getConnectionData(this)
            }
            catch (exception: Exception) {
                Log.w(TAG, "Failed to get connection data!")
                label = getString(R.string.qs_tile_label_unavailable)
                message = getString(R.string.qs_tile_subtitle_connection_data_error)
            }

            qsTile.state = Tile.STATE_ACTIVE
            qsTile.label = label
            qsTile.subtitle = message
        }
        else {
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.label = getString(R.string.qs_tile_label_disabled)
            qsTile.subtitle = getString(R.string.qs_tile_subtitle_basic)
        }

        qsTile.updateTile()
    }

}
