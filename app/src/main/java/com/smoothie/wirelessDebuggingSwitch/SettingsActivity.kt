package com.smoothie.wirelessDebuggingSwitch

import android.content.Intent
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceManager
import com.android.settingslib.PrimarySwitchPreference
import com.smoothie.widgetFactory.preference.PreferenceActivity
import com.smoothie.widgetFactory.preference.PreferenceFragment
import com.smoothie.wirelessDebuggingSwitch.preference.ActivityPreferenceKdeConnect
import com.smoothie.wirelessDebuggingSwitch.preference.ActivityPreferencePrefix

class SettingsActivity : PreferenceActivity(R.xml.preferences_app, R.string.app_name) {

    private lateinit var preferenceKdeConnect: PrimarySwitchPreference
    private lateinit var preferencePrefixData: PrimarySwitchPreference

    private val kdeConnectSummaryProvider =
        SummaryProvider<PrimarySwitchPreference> { preference ->
            if (!KdeConnect.isInstalled(this))
                return@SummaryProvider getString(R.string.preference_summary_need_kde_connect)

            val manager = PreferenceManager.getDefaultSharedPreferences(this)
            if (manager.getBoolean(preference.key, true))
                return@SummaryProvider getString(R.string.state_enabled)
            return@SummaryProvider getString(R.string.state_disabled)
        }

    private val prefixConnectionDataSummaryProvider =
        SummaryProvider<PrimarySwitchPreference> { preference ->
            if (!KdeConnect.isInstalled(this))
                return@SummaryProvider getString(R.string.preference_summary_need_kde_connect)

            val manager = PreferenceManager.getDefaultSharedPreferences(this)

            if (!manager.getBoolean(preference.key, true))
                return@SummaryProvider getString(R.string.state_disabled)

            return@SummaryProvider manager.getString(
                    getString(R.string.key_connection_data_prefix),
                    getString(R.string.default_connection_data_prefix)
                )
        }

    override fun onResume() {
        super.onResume()

        if (!this::preferenceKdeConnect.isInitialized || !this::preferencePrefixData.isInitialized)
            return

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        var value = preferences.getBoolean(preferenceKdeConnect.key, false)
        preferenceKdeConnect.isChecked = value

        value = preferences.getBoolean(preferencePrefixData.key, false)
        preferencePrefixData.isChecked = value
    }

    override fun onPreferencesCreated(preferenceFragment: PreferenceFragment) {
        super.onPreferencesCreated(preferenceFragment)
        GrantPermissionsActivity.startIfNeeded(this)

        val kdeConnectInstalled = KdeConnect.isInstalled(this)
        preferenceKdeConnect = preferenceFragment.findPreference(getString(R.string.key_enable_kde_connect))!!
        preferenceKdeConnect.isEnabled = kdeConnectInstalled
        preferenceKdeConnect.summaryProvider = kdeConnectSummaryProvider
        preferenceKdeConnect.onPreferenceClickListener = OnPreferenceClickListener {
            startActivity(Intent(baseContext, ActivityPreferenceKdeConnect::class.java))
            false
        }

        val prefixDataPreferenceKey = getString(R.string.key_prefix_connection_data)
        preferencePrefixData = preferenceFragment.findPreference(prefixDataPreferenceKey)!!
        preferencePrefixData.isEnabled = kdeConnectInstalled
        preferencePrefixData.summaryProvider = prefixConnectionDataSummaryProvider
        preferencePrefixData.setOnPreferenceClickListener {
            startActivity(Intent(baseContext, ActivityPreferencePrefix::class.java))
            false
        }

    }

}
