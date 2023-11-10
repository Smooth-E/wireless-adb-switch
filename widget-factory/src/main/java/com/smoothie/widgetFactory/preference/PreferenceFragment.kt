package com.smoothie.widgetFactory.preference

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat

open class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val preferenceActivity = requireActivity() as PreferenceActivity

        val resource = preferenceActivity.preferencesResourceId
        setPreferencesFromResource(resource, rootKey)

        preferenceActivity.onPreferencesCreated(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as PreferenceActivity)
            .onPreferenceFragmentViewCreated(view, savedInstanceState)
    }

}
