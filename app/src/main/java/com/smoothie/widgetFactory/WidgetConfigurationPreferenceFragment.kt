package com.smoothie.widgetFactory

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat

class WidgetConfigurationPreferenceFragment(
    private val preferenceResource: Int,
    private val sharedPreferencesName: String,
    private val listener: SharedPreferences.OnSharedPreferenceChangeListener
) : PreferenceFragmentCompat() {

    companion object {
        private const val TAG = "WidgetConfigurationPreferenceFragment"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = sharedPreferencesName
        setPreferencesFromResource(preferenceResource, rootKey)

        val preferences = preferenceManager.sharedPreferences

        if (preferences == null) {
            Log.d(TAG, "preferenceManager.sharedPreferences is null!")
            requireActivity().finish()
            return
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)

        // This will generate widget preview on activity startup
        listener.onSharedPreferenceChanged(preferences, rootKey)
    }

    override fun onDestroy() {
        super.onDestroy()

        preferenceManager
            .sharedPreferences?.unregisterOnSharedPreferenceChangeListener(listener)
    }

}
