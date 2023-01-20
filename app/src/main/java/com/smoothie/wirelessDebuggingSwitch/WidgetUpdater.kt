package com.smoothie.wirelessDebuggingSwitch

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class WidgetUpdater : BroadcastReceiver() {

    companion object {

        private const val INTENT_ACTION =
            "com.smoothie.wirelessDebuggingSwitch.intent.WIDGET_UPDATE_TICK"
        private const val INTERVAL = 1 * 1000
        private const val TAG = "WidgetUpdater"

        private var lastPendingIntent: PendingIntent? = null
        private var enabled = false

        private fun getAlarmManager(context: Context): AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        private fun createPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, WidgetUpdater::class.java)
            intent.action = INTENT_ACTION

            val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            return PendingIntent.getBroadcast(context, 0, intent, flag)
        }

        private fun schedule(context: Context) {
            Log.d("WidgetUpdater", "enableOrUpdate() called!")
            val time = System.currentTimeMillis() + INTERVAL
            val intent = createPendingIntent(context)
            getAlarmManager(context).setExact(AlarmManager.RTC, time, intent)
            lastPendingIntent = intent
        }

        fun enable(context: Context) {
            if (enabled) {
                Log.d(TAG, "Already enabled!")
                return
            }

            schedule(context)
            enabled = true
        }

        fun disable(context: Context) {
            if (lastPendingIntent == null) {
                Log.d("WidgetUpdater", "lastPendingIntent was null on disable()")
                return
            }

            val alarmManager = getAlarmManager(context)
            alarmManager.cancel(lastPendingIntent)
        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == INTENT_ACTION) {
            schedule(context!!)
            context.sendBroadcast(SwitchWidget.createUpdateIntent(context))
        }
    }

}
