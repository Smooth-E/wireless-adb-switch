package com.smoothie.wirelessDebuggingSwitch

import androidx.preference.Preference.OnPreferenceClickListener
import com.android.settingslib.PrimarySwitchPreference
import com.smoothie.widgetFactory.preference.PreferenceActivity
import com.smoothie.widgetFactory.preference.PreferenceFragment

class SettingsActivity : PreferenceActivity(R.xml.app_preferences, R.string.app_name) {

    override fun onPreferencesCreated(preferenceFragment: PreferenceFragment) {
        super.onPreferencesCreated(preferenceFragment)
        GrantPermissionsActivity.startIfNeeded(this)

        val preferenceKdeConnect = preferenceFragment
            .findPreference<PrimarySwitchPreference>(getString(R.string.key_enable_kde_connect))!!
        preferenceKdeConnect.isEnabled = KdeConnect.isInstalled(this)
        preferenceKdeConnect.onPreferenceClickListener = OnPreferenceClickListener {
            // TODO: Open an activity with extensive feature description
            false
        }

        // TODO: Open an activity with extensive feature description for Prefix synced data

    }

}
