package com.smoothie.wirelessDebuggingSwitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.preference.PreferenceManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OnBootCompletedReceiver : BroadcastReceiver() {

    private val tag = "OnBootCompletedReceiver"
    private val defaultWaitingTime = 30f // in seconds

    private var actionReceived = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) {
            Log.w(tag, "onReceive: Action does not match boot completed")
            return
        }

        if (context == null) {
            Log.e(tag, "onReceive: Context is null")
            return
        }

        if (actionReceived) {
            Log.d(tag, "onReceive: Action already ran")
            return
        }

        Log.d(tag, "onReceive: Proceeding")
        actionReceived = true

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val keyEnableOnBoot = context.getString(R.string.key_enable_on_boot)
        if (!preferences.getBoolean(keyEnableOnBoot, false)) {
            Log.d(tag, "onReceive: Enable on but is disabled")
            return
        }

        val waitingTimeKey = context.getString(R.string.key_after_boot_wait_time)
        val waitingTime = preferences.getFloat(waitingTimeKey, defaultWaitingTime)

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(tag, "Callback: network became available")

                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) != true) {
                    Log.d(tag, "Callback: WiFi transport type is unavailable")
                    return
                }

                Log.d(tag, "Callback enabling wireless debugging")
                WirelessDebugging.setEnabled(context, true)
                WirelessDebugging.syncConnectionData(context)
                Log.d(tag, "Callback: done")

                connectivityManager.unregisterNetworkCallback(this)
                Log.d(tag, "Callback: unregistered the callback: done")
            }
        }

        connectivityManager.registerNetworkCallback(request, callback)
        Log.d(tag, "onReceive: Assigned the network callback")

        val scheduler = Executors.newSingleThreadScheduledExecutor()
        scheduler.schedule({
            connectivityManager.unregisterNetworkCallback(callback)
            Log.d(tag, "Scheduler: callback unregistered: timeout")
        }, waitingTime.toLong(), TimeUnit.SECONDS)
        Log.d(tag, "onReceive: Scheduled to unregister the callback in $waitingTime seconds")
    }

}
