package com.smoothie.wirelessDebuggingSwitch

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.net.wifi.WifiManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.PendingIntentCompat
import androidx.core.content.ContextCompat
import com.smoothie.wirelessDebuggingSwitch.WifiReceiver.Companion.OnWifiStateChangeListener

class QSTileService : TileService() {

    companion object {
        private const val TAG = "QSTileService"
    }

    private lateinit var wifiReceiver: WifiReceiver

    private val wifiStateChangeListener = object : OnWifiStateChangeListener {
        override fun onWifiStateChanged(currentState: Int, previousState: Int) =
            updateTile()
    }

    override fun onCreate() {
        super.onCreate()
        registerWifiReceiver()
        WifiReceiver.addOnWifiStateChangeListener(wifiStateChangeListener)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "Tile onClick() called!")
        switchState()
    }

    override fun onDestroy() {
        super.onDestroy()

        WifiReceiver.removeOnWifiStateChangeListener(wifiStateChangeListener)

        try {
            unregisterReceiver(wifiReceiver)
        }
        catch (expected: IllegalArgumentException) {
            Log.w(TAG, "Failed to unregister wifiReceiver!")
        }
    }

    /*
     * Why @SuppressLint("WrongConstant")?
     * Android Studio reports ContextCompat.RECEIVER_NOT_EXPORTED as a wrong constant even tho it is
     * stated to use it in the documentation.
     * https://developer.android.com/guide/components/broadcasts#context-registered-receivers
     * 'Context.RECEIVER_NOT_EXPORTED' however is only accessible on API 33+
     */
    @SuppressLint("WrongConstant")
    private fun registerWifiReceiver() {
        wifiReceiver = WifiReceiver()
        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)

        val flags =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                ContextCompat.RECEIVER_NOT_EXPORTED
            else
                Context.RECEIVER_NOT_EXPORTED

        registerReceiver(wifiReceiver, intentFilter, flags)
    }

    private fun switchState() {
        WirelessDebugging.setEnabled(this, !WirelessDebugging.getEnabled(this))
        updateTile()

        if (WirelessDebugging.getEnabled(this))
            WirelessDebugging.syncConnectionData(this)
    }

    private fun isWifiEnabled(): Boolean {
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    private fun updateTile() {
        Log.d(TAG, "Tile is being updated!")

        if (!isWifiEnabled()) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.icon = Icon.createWithResource(this, R.drawable.baseline_wifi_off_24)
            qsTile.label = getString(R.string.qs_tile_label_unavailable)
            qsTile.subtitle = getString(R.string.qs_tile_subtitle_enable_wifi_first)
            qsTile.updateTile()
            return
        }

        if (!hasSufficientPrivileges()) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.icon = Icon.createWithResource(this, R.drawable.round_phonelink_setup_24)
            qsTile.label = getString(R.string.qs_tile_label_missing_privileges)
            qsTile.subtitle = getString(R.string.qs_tile_subtitle_missing_privileges)
            qsTile.updateTile()
            return
        }

        qsTile.icon = Icon.createWithResource(this, R.drawable.baseline_phonelink_24)

        if (WirelessDebugging.getEnabled(this)) {
            var label: String
            var message: String

            try {
                label = getString(R.string.qs_tile_label_enabled)
                message = WirelessDebugging.getConnectionData(this)
            }
            catch (exception: Exception) {
                Log.w(TAG, "Failed to get connection data!")
                label = getString(R.string.qs_tile_label_unavailable)
                message = getString(R.string.qs_tile_subtitle_no_connection_data)
            }

            qsTile.state = Tile.STATE_ACTIVE
            qsTile.label = label
            qsTile.subtitle = message
        }
        else {
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.label = getString(R.string.qs_tile_label_disabled)
            qsTile.subtitle = getString(R.string.qs_tile_subtitle_hold_for_settings)
        }

        qsTile.updateTile()

        // Update the detail view for Samsung's OneUI hidden APIs
        try {
            javaClass.getMethod("semUpdateDetailView").invoke(this)
        }
        catch (exception: Exception) {
            Log.d(javaClass.name, "Unable to call semUpdateDetailView(...)!")
        }
    }

    // Integration with Samsung's OneUI hidden APIs described at
    // https://oneuiproject.github.io/hidden-api/qs-detail-view/

    fun semGetDetailViewTitle(): CharSequence =
        getString(R.string.label_wireless_debugging)

    fun semIsToggleButtonExists(): Boolean =
        hasSufficientPrivileges() && isWifiEnabled()

    // This is called only once to get the initial state of the switch
    fun semIsToggleButtonChecked(): Boolean =
        WirelessDebugging.getEnabled(this)

    @SuppressLint("WrongConstant")
    fun semGetDetailView(): RemoteViews {
        Log.d(TAG, "semGetDetailView()")
        val views: RemoteViews
        val hasSufficientPrivileges = hasSufficientPrivileges()
        val wifiEnabled = isWifiEnabled()
        val wirelessDebuggingEnabled = WirelessDebugging.getEnabled(this)

        if (wifiEnabled && hasSufficientPrivileges && wirelessDebuggingEnabled) {
            views = RemoteViews(packageName, R.layout.view_qs_details)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            val connectionData = WirelessDebugging.getConnectionData(this)
            views.setTextViewText(R.id.text_connection_data, connectionData)

            val copyDataIntent = Intent(ExtendedViewBroadcastReceiver.ACTION_COPY_DATA)
            val copyDataPendingIntent =
                PendingIntent.getBroadcast(this, 0, copyDataIntent, flags)
            views.setOnClickPendingIntent(R.id.button_copy_connection_data, copyDataPendingIntent)

            val settingsIntent = Intent(baseContext, SettingsActivity::class.java)
                .setFlags(
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

            val settingsPendingIntent = PendingIntentCompat
                .getActivity(this, 1, settingsIntent, 0, false)
            views.setOnClickPendingIntent(R.id.group_more_setting_in_app, settingsPendingIntent)

            if (!KdeConnect.isClipboardSharingAvailable(this)) {
                views.setViewVisibility(R.id.group_instant_connection, View.GONE)
                return views
            }

            val instantConnectionIntent =
                Intent(ExtendedViewBroadcastReceiver.ACTION_INSTANT_CONNECTION)
            val instantConnectionPendingIntent = PendingIntent
                .getBroadcast(this, 2, instantConnectionIntent, flags)
            views.setOnClickPendingIntent(
                R.id.button_instant_connection,
                instantConnectionPendingIntent
            )
        }
        else {
            views = RemoteViews(packageName, R.layout.view_qs_error)
            if (!hasSufficientPrivileges) {
                views.setImageViewResource(R.id.icon, R.drawable.round_phonelink_setup_24)
                views.setTextViewText(R.id.heading, getString(R.string.notification_title))
                views.setTextViewText(R.id.body, getString(R.string.notification_message_long))
            }
            else if (!wifiEnabled) {
                views.setImageViewResource(R.id.icon, R.drawable.baseline_wifi_off_24)
                views.setTextViewText(R.id.heading, getString(R.string.title_wifi_disabled))
                views.setTextViewText(R.id.body, getString(R.string.message_wifi_disabled))
            }
            else { // Wireless debugging is disabled
                views.setImageViewResource(R.id.icon, R.drawable.app_icon_foreground)
                views.setTextViewText(R.id.heading, getString(R.string.title_debugging_disabled))
                views.setTextViewText(R.id.body, getString(R.string.message_debugging_disabled))
            }
        }

        return views
    }

    // This will get called each time you toggle the switch-bar and therefore serves as listener.
    fun semSetToggleButtonChecked(checked: Boolean) {
        switchState()
        updateTile()

        Log.d(TAG, "semSetToggleButtonChecked(...)")

        // Set switch state of the top bar to the actual value
        // of whether the Wireless Debugging is now enabled

        val debuggingEnabled = WirelessDebugging.getEnabled(this)

        if (debuggingEnabled == checked)
            return

        try {
            javaClass.getMethod(
                "semFireToggleStateChanged",
                Boolean.Companion::class.java,
                Boolean.Companion::class.java
            ).invoke(this, debuggingEnabled, semIsToggleButtonExists())
        }
        catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

}
