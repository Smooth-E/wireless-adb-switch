package com.smoothie.widgetFactory.configuration

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.smoothie.wirelessDebuggingSwitch.R

class PreferenceFragment : PreferenceFragmentCompat() {

    companion object {
        private const val TAG = "WidgetConfigurationPreferenceFragment"

        const val KEY_PREFERENCES_NAME = "PREFERENCES_NAME"
        const val KEY_PREFERENCES_RESOURCE = "PREFERENCES_RESOURCE"
        const val KEY_PREVIEW_ASPECT = "PREVIEW_ASPECT"
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

        val previewAspectRatio = requireArguments().getFloat(KEY_PREVIEW_ASPECT)
        Log.d(TAG, "Preview aspect ratio: $previewAspectRatio")
        listener = OnConfigurationChangedListener(
            configurationActivity,
            previewViewGroup,
            previewAspectRatio
        )

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
