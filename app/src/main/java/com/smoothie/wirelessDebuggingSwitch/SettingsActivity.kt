package com.smoothie.wirelessDebuggingSwitch

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceManager
import com.android.settingslib.PrimarySwitchPreference
import com.smoothie.widgetFactory.ApplicationPreferenceActivity
import com.smoothie.widgetFactory.preference.PreferenceFragment
import com.smoothie.wirelessDebuggingSwitch.preference.ActivityPreferenceKdeConnect
import com.smoothie.wirelessDebuggingSwitch.preference.ActivityPreferencePrefix

class SettingsActivity : ApplicationPreferenceActivity(
    R.xml.preferences_app,
    R.string.app_name
) {

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
                return@SummaryProvider getString(R.string.preference_summary_need_kde_connect_integration)

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

        var value = preferences.getBoolean(preferenceKdeConnect.key, true)
        preferenceKdeConnect.isChecked = value

        value = preferences.getBoolean(preferencePrefixData.key, true)
        preferencePrefixData.isChecked = value

        // This will update the summary using the previously set SummaryProvider
        preferencePrefixData.forceUpdate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GrantPermissionsActivity.startIfNeeded(this)
    }

    override fun onPreferencesCreated(preferenceFragment: PreferenceFragment) {
        super.onPreferencesCreated(preferenceFragment)

        val kdeConnectInstalled = KdeConnect.isInstalled(this)
        var preferenceKey: String

        preferenceKey = getString(R.string.key_enable_kde_connect)
        preferenceKdeConnect = preferenceFragment.findPreference(preferenceKey)!!
        preferenceKdeConnect.isEnabled = kdeConnectInstalled
        preferenceKdeConnect.summaryProvider = kdeConnectSummaryProvider
        preferenceKdeConnect.onPreferenceClickListener = OnPreferenceClickListener {
            startActivity(Intent(baseContext, ActivityPreferenceKdeConnect::class.java))
            false
        }

        preferenceKey = getString(R.string.key_prefix_connection_data)
        preferencePrefixData = preferenceFragment.findPreference(preferenceKey)!!
        preferencePrefixData.isEnabled = kdeConnectInstalled
        preferencePrefixData.summaryProvider = prefixConnectionDataSummaryProvider
        preferencePrefixData.setOnPreferenceClickListener {
            startActivity(Intent(baseContext, ActivityPreferencePrefix::class.java))
            false
        }

        preferenceKey = getString(R.string.key_app_version)
        val preferenceAppVersion = preferenceFragment.findPreference<Preference>(preferenceKey)!!
        preferenceAppVersion.summary = BuildConfig.VERSION_NAME
        preferenceAppVersion.setOnPreferenceClickListener { _ ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(
                getString(R.string.preference_name_app_version),
                BuildConfig.VERSION_NAME
            ))
            Toast.makeText(this, R.string.message_copied, Toast.LENGTH_SHORT).show()
            return@setOnPreferenceClickListener false
        }
    }

}
