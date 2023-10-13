package com.smoothie.wirelessDebuggingSwitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log

/**
 * Receives broadcasts when the "Restart app" button is clicked on the notification.
 * It just kills the app instead of restarting in contrast to how it is done in
 * [GrantPermissionsActivity.GrantPermissionsFragment.restartAppForRootAccessRefresh].
 * This is because the app will be most likely restarted by the alarm set by the widget updater.
 */
class KillAppBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val INTENT_ACTION = "com.smoothie.wadbs.KILL_APP"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.e(javaClass.name, "Context iss null! Returning.")
            return
        }

        Log.d(javaClass.name, "App is killed!")
        Process.killProcess(Process.myPid())
    }

}
