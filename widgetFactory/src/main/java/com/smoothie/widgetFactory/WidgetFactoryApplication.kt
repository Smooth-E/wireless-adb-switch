package com.smoothie.widgetFactory

import android.app.Application
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.Log
import androidx.preference.PreferenceManager
import com.smoothie.widgetFactory.WidgetUpdater

open class WidgetFactoryApplication : Application() {

    private lateinit var sharedPreferenceChangeListener: OnSharedPreferenceChangeListener

    override fun onCreate() {
        super.onCreate()

        Log.d("WidgetFactoryApplication", "Application created!")

        sharedPreferenceChangeListener =
            WidgetUpdater.OnSharedPreferencesChangeListener(this)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        WidgetUpdater.enable(this)
    }

}
