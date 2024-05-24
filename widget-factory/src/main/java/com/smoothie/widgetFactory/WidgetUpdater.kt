package com.smoothie.widgetFactory

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager

class WidgetUpdater : BroadcastReceiver() {

    class OnSharedPreferencesChangeListener(
        private val context: Context
    ) : SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            val keyUpdateInterval = context.getString(R.string.key_update_interval)
            val keyUpdatesEnabled = context.getString(R.string.key_updates_enabled)

            if (key != keyUpdateInterval && key != keyUpdatesEnabled)
                return

            Log.d("WidgetUpdaterSPListener", "Preference updated!")
            disable(context)

            if (sharedPreferences?.getBoolean(keyUpdatesEnabled, true) == true)
                enable(context)
        }

    }

    companion object {

        private const val INTENT_ACTION = "com.smoothie.widgetFactory.WIDGET_UPDATE_TICK_ACTION"
        private const val BOOT_COMPLETED_ACTION = Intent.ACTION_BOOT_COMPLETED
        private const val TAG = "WidgetUpdater"

        private var interval = 1
        private var lastPendingIntent: PendingIntent? = null

        var enabled = false
            private set

        private fun getAlarmManager(context: Context): AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        private fun createPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, WidgetUpdater::class.java)
            intent.action = INTENT_ACTION

            val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            return PendingIntent.getBroadcast(context, 0, intent, flag)
        }

        private fun schedule(context: Context) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val key = context.getString(R.string.key_update_interval)
            interval = sharedPreferences.getFloat(key, 1f).toInt()

            val time = System.currentTimeMillis() + interval * 1000

            val intent = createPendingIntent(context)
            getAlarmManager(context).setExact(AlarmManager.RTC, time, intent)
            lastPendingIntent = intent

            Log.d(TAG, "Scheduled alarm for $interval seconds")
        }

        fun forceUpdate(context: Context) {
            Log.d(TAG, "forceUpdate()")
            createPendingIntent(context).send()
        }

        fun enable(context: Context) {
            if (enabled) {
                Log.d(TAG, "Already enabled!")
                return
            }

            schedule(context)
            enabled = true
            Log.d(TAG, "Enabled")
        }

        fun disable(context: Context) {
            if (lastPendingIntent == null) {
                Log.d(TAG, "lastPendingIntent was null on disable()")
                return
            }

            val alarmManager = getAlarmManager(context)
            alarmManager.cancel(lastPendingIntent!!)
            enabled = false
            Log.d(TAG, "Disabled")
        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == INTENT_ACTION || intent?.action == BOOT_COMPLETED_ACTION) {
            ConfigurableWidget.updateAllWidgets(context!!)

            if (enabled)
                schedule(context)
        }
    }

}
