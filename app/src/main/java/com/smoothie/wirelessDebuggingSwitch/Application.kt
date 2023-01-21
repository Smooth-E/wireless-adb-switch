package com.smoothie.wirelessDebuggingSwitch

import android.app.Application
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.Log
import androidx.preference.PreferenceManager
import com.smoothie.wirelessDebuggingSwitch.receiver.WidgetUpdater

class CustomApplication : Application() {

    private lateinit var sharedPreferenceChangeListener: OnSharedPreferenceChangeListener

    override fun onCreate() {
        super.onCreate()

        Log.d("Application", "Application created!")

        sharedPreferenceChangeListener =
            WidgetUpdater.OnSharedPreferencesChangeListener(this)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        WidgetUpdater.enable(this)
    }

}
