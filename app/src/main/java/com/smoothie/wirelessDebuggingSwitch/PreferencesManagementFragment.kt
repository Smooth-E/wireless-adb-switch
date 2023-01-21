package com.smoothie.wirelessDebuggingSwitch

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class PreferencesManagementFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
        setPreferencesFromResource(R.xml.app_preferences, rootKey)
}