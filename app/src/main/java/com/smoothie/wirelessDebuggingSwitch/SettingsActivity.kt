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
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.PrimarySwitchPreference
import com.smoothie.widgetFactory.ApplicationPreferenceActivity
import com.smoothie.widgetFactory.preference.PreferenceFragment
import com.smoothie.wirelessDebuggingSwitch.preference.ActivityPreferenceKdeConnect
import com.smoothie.wirelessDebuggingSwitch.preference.ActivityPreferencePrefix

class SettingsActivity : ApplicationPreferenceActivity(
    R.xml.preferences_app,
    R.string.app_name
) {

    private lateinit var preferenceCopyData: SwitchPreferenceCompat
    private lateinit var preferencePrefixData: PrimarySwitchPreference
    private lateinit var preferenceKdeConnect: PrimarySwitchPreference

    private val kdeConnectSummaryProvider =
        SummaryProvider<PrimarySwitchPreference> { preference ->
            if (!hasSufficientPrivileges(PrivilegeLevel.Root))
                return@SummaryProvider getString(R.string.preference_summary_root_for_kde_connect)

            if (!KdeConnect.isInstalled(this))
                return@SummaryProvider getString(R.string.preference_summary_need_kde_connect)

            if (!preferenceCopyData.isChecked)
                return@SummaryProvider getString(R.string.preference_summary_need_copying_data)

            val manager = PreferenceManager.getDefaultSharedPreferences(this)
            if (manager.getBoolean(preference.key, true))
                return@SummaryProvider getString(R.string.state_enabled)
            return@SummaryProvider getString(R.string.state_disabled)
        }

    private val prefixConnectionDataSummaryProvider =
        SummaryProvider<PrimarySwitchPreference> { preference ->
            if (!preferenceCopyData.isChecked)
                return@SummaryProvider getString(R.string.preference_summary_need_copying_data)

            val manager = PreferenceManager.getDefaultSharedPreferences(this)

            if (!manager.getBoolean(preference.key, true))
                return@SummaryProvider getString(R.string.state_disabled)

            return@SummaryProvider manager.getString(
                    getString(R.string.key_connection_data_prefix),
                    getString(R.string.default_connection_data_prefix)
                )
        }

        private val appVersionOnPreferenceClickListener = OnPreferenceClickListener { _ ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(
                getString(R.string.preference_name_app_version),
                BuildConfig.VERSION_NAME
            ))
            Toast.makeText(this, R.string.message_copied, Toast.LENGTH_SHORT).show()
            return@OnPreferenceClickListener false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GrantPermissionsActivity.startIfNeeded(this)
    }

    override fun onPreferencesCreated(preferenceFragment: PreferenceFragment) {
        super.onPreferencesCreated(preferenceFragment)

        // Right now, integration with KDE Connect requires root privileges,
        // because we need to start a non-exported activity to share clipboard

        val kdeConnectInstalled = KdeConnect.isInstalled(this)
        val hasRoot = hasSufficientPrivileges(PrivilegeLevel.Root)

        var preferenceKey = getString(R.string.key_copy_connection_data)
        preferenceCopyData = preferenceFragment.findPreference(preferenceKey)!!

        preferenceKey = getString(R.string.key_prefix_connection_data)
        preferencePrefixData = preferenceFragment.findPreference(preferenceKey)!!
        preferencePrefixData.summaryProvider = prefixConnectionDataSummaryProvider
        preferencePrefixData.setOnPreferenceClickListener {
            startActivity(Intent(baseContext, ActivityPreferencePrefix::class.java))
            false
        }

        preferenceKey = getString(R.string.key_enable_kde_connect)
        preferenceKdeConnect = preferenceFragment.findPreference(preferenceKey)!!
        preferenceKdeConnect.isEnabled = kdeConnectInstalled && hasRoot
        preferenceKdeConnect.summaryProvider = kdeConnectSummaryProvider
        preferenceKdeConnect.onPreferenceClickListener = OnPreferenceClickListener {
            startActivity(Intent(baseContext, ActivityPreferenceKdeConnect::class.java))
            false
        }

        preferenceKey = getString(R.string.key_app_version)
        val preferenceAppVersion = preferenceFragment.findPreference<Preference>(preferenceKey)!!
        preferenceAppVersion.summary = BuildConfig.VERSION_NAME
        preferenceAppVersion.onPreferenceClickListener = appVersionOnPreferenceClickListener
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
        preferenceKdeConnect.forceUpdate()
        preferencePrefixData.forceUpdate()
    }

}
