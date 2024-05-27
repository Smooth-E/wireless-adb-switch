package com.smoothie.widgetFactory

import android.app.Application
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.Log
import androidx.preference.PreferenceManager

open class WidgetFactoryApplication : Application() {

    private lateinit var sharedPreferenceChangeListener: OnSharedPreferenceChangeListener

    override fun onCreate() {
        super.onCreate()

        Log.d("WidgetFactoryApplication", "Application created!")

        sharedPreferenceChangeListener =
            WidgetUpdater.OnSharedPreferencesChangeListener(this)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        if (preferences.getBoolean(getString(R.string.key_updates_enabled), true))
            WidgetUpdater.enable(this)
        else
            WidgetUpdater.forceUpdate(this)
    }

}
