package com.smoothie.widgetFactory.preference

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

open class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val preferenceActivity = requireActivity() as PreferenceActivity

        val resource = preferenceActivity.preferencesResourceId
        setPreferencesFromResource(resource, rootKey)

        preferenceActivity.onPreferencesCreated(this)
    }

}
