package com.smoothie.widgetFactory

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

open class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val resource = (requireActivity() as PreferenceActivity).preferencesResourceId
        setPreferencesFromResource(resource, rootKey)
    }

}
