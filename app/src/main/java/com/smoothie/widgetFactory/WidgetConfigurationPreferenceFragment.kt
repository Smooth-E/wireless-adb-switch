package com.smoothie.widgetFactory

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.smoothie.wirelessDebuggingSwitch.R

class WidgetConfigurationPreferenceFragment : PreferenceFragmentCompat() {

    companion object {
        private const val TAG = "WidgetConfigurationPreferenceFragment"

        const val KEY_PREFERENCES_NAME = "PREFERENCES_NAME"
        const val KEY_PREFERENCES_RESOURCE = "PREFERENCES_RESOURCE"
    }

    private lateinit var listener: OnSharedPreferenceChangeListener
    private var preferencesRootKey: String? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferencesRootKey = rootKey

        val preferencesName = requireArguments().getString(KEY_PREFERENCES_NAME)
        preferenceManager.sharedPreferencesName = preferencesName

        val preferencesResourceId = requireArguments().getInt(KEY_PREFERENCES_RESOURCE)
        setPreferencesFromResource(preferencesResourceId, rootKey)

        val preferences = preferenceManager.sharedPreferences

        if (preferences == null) {
            Log.d(TAG, "preferenceManager.sharedPreferences is null!")
            requireActivity().finish()
            return
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val configurationActivity = requireActivity() as WidgetConfigurationActivity
        val previewViewGroup = configurationActivity.findViewById<ViewGroup>(R.id.preview_holder)

        listener = OnWidgetConfigurationChangedListener(configurationActivity, previewViewGroup)

        val preferences = preferenceManager.sharedPreferences!!
        preferences.registerOnSharedPreferenceChangeListener(listener)

        // This will generate widget preview on activity startup
        listener.onSharedPreferenceChanged(preferences, preferencesRootKey)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(listener)
    }

}
