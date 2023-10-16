package com.smoothie.wirelessDebuggingSwitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/** A broadcast receiver for actions in the One UI Extended Quick Settings View */
class ExtendedViewBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ExtendedViewBroadcastReceiver"
        const val ACTION_COPY_DATA = "com.smoothie.wadbs.oneui_qs_action.COPY_DATA"
        const val ACTION_INSTANT_CONNECTION =
            "com.smoothie.wadbs.oneui_qs_action.INSTANT_CONNECTION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Broadcast received")
        context ?: return
        val action = intent?.action ?: return
        val message: String
        Log.d(TAG, "Received $action")
        when (action) {
            ACTION_COPY_DATA -> {
                WirelessDebugging.copyConnectionData(context)
                message = context.getString(R.string.message_copied)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            ACTION_INSTANT_CONNECTION -> {
                WirelessDebugging.syncConnectionData(context)
            }
        }
    }
}
