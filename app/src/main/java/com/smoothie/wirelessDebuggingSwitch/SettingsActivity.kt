package com.smoothie.wirelessDebuggingSwitch

import androidx.preference.SwitchPreferenceCompat
import com.smoothie.widgetFactory.preference.PreferenceActivity
import com.smoothie.widgetFactory.preference.PreferenceFragment

class SettingsActivity : PreferenceActivity(R.xml.app_preferences, R.string.app_name) {

    override fun onPreferencesCreated(preferenceFragment: PreferenceFragment) {
        super.onPreferencesCreated(preferenceFragment)
        GrantPermissionsActivity.startIfNeeded(this)

        val keyKdeConnectIntegration = getString(R.string.key_enable_kde_connect)
        val keyPrefixData = getString(R.string.key_prefix_connection_data)

        val preferenceKdeConnect =
            preferenceFragment.findPreference<SwitchPreferenceCompat>(keyKdeConnectIntegration)
        preferenceKdeConnect!!.isEnabled = KdeConnect.isInstalled(this)

        val preferencePrefixData =
            preferenceFragment.findPreference<SwitchPreferenceCompat>(keyPrefixData)!!
        val status = preferenceKdeConnect.isEnabled && preferenceKdeConnect.isChecked
        preferencePrefixData.isEnabled = status
    }

}
