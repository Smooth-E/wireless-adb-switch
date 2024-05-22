package com.smoothie.wirelessDebuggingSwitch

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceGroup
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

    companion object {
        private const val TAG = "SettingsActivityLogs"
    }

    private lateinit var preferenceDebuggingEnabled: SwitchPreferenceCompat
    private lateinit var preferenceConnectionDetails: Preference
    private lateinit var preferenceServiceActive: SwitchPreferenceCompat
    private lateinit var preferenceGroupSettings: PreferenceGroup
    private lateinit var preferenceCopyData: SwitchPreferenceCompat
    private lateinit var preferencePrefixData: PrimarySwitchPreference
    private lateinit var preferenceKdeConnect: PrimarySwitchPreference

    private var debuggingStatusThread: DebuggingStatusThread? = null

    private val debuggingEnabledClickListener = OnPreferenceClickListener {
        WirelessDebugging.setEnabled(this, !WirelessDebugging.getEnabled(this))
        updateDebuggingConnectionDetails()
        false
    }

    private val connectionDetailsClickListener = OnPreferenceClickListener {
        val label = getString(R.string.preference_name_connection_details)
        val text = WirelessDebugging.getConnectionData(this)
        copyWithToast(label, text)
        false
    }

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
        copyWithToast(getString(R.string.preference_name_app_version), BuildConfig.VERSION_NAME)
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GrantPermissionsActivity.startIfNeeded(this)
    }

    override fun onPreferencesCreated(preferenceFragment: PreferenceFragment) {
        super.onPreferencesCreated(preferenceFragment)

        Log.d(TAG, "onPreferencesCreated")

        var preferenceKey = getString(R.string.key_debugging_enabled)
        preferenceDebuggingEnabled = preferenceFragment.findPreference(preferenceKey)!!
        preferenceDebuggingEnabled.onPreferenceClickListener = debuggingEnabledClickListener

        preferenceKey = getString(R.string.key_connection_details)
        preferenceConnectionDetails = preferenceFragment.findPreference(preferenceKey)!!
        preferenceConnectionDetails.onPreferenceClickListener = connectionDetailsClickListener

        preferenceKey = getString(com.smoothie.widgetFactory.R.string.key_updates_enabled)
        preferenceServiceActive = preferenceFragment.findPreference(preferenceKey)!!

        preferenceKey = getString(R.string.key_settings_preference_group)
        preferenceGroupSettings = preferenceFragment.findPreference(preferenceKey)!!

        // Right now, integration with KDE Connect requires root privileges,
        // because we need to start a non-exported activity to share clipboard

        val kdeConnectInstalled = KdeConnect.isInstalled(this)
        val hasRoot = hasSufficientPrivileges(PrivilegeLevel.Root)

        preferenceKey = getString(R.string.key_copy_connection_data)
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

        updateDebuggingConnectionDetails()
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "onResume()")

        debuggingStatusThread = DebuggingStatusThread(this)
        debuggingStatusThread?.start()

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

    override fun onPause() {
        super.onPause()

        if (debuggingStatusThread != null) {
            debuggingStatusThread!!.interrupt()
            debuggingStatusThread = null
        }
    }

    private fun updateDebuggingConnectionDetails() {
        val enabled = WirelessDebugging.getEnabled(this)

        preferenceDebuggingEnabled.isChecked = enabled
        preferenceConnectionDetails.isVisible = enabled

        if (enabled) {
            val connectionData = WirelessDebugging.getConnectionData(this)
            val message = getString(R.string.preference_summary_connection_details)
            preferenceConnectionDetails.summary = "$message $connectionData"
        }

        Log.d(TAG, "Debugging details updated: $enabled")
    }

    private fun copyWithToast(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(this, R.string.message_copied, Toast.LENGTH_SHORT).show()
    }

    private class DebuggingStatusThread(private val settingsActivity: SettingsActivity) : Thread() {

        val tag: String = "SettingsActivity.DebuggingStatusThread"
        val interval: Long = 1000

        override fun run() {
            while (!isInterrupted) {
                try {
                    settingsActivity.runOnUiThread {
                        settingsActivity.updateDebuggingConnectionDetails()
                        Log.d(tag, "Debugging switch status updated")
                    }
                    sleep(interval)
                }
                catch (exception: InterruptedException) {
                    Log.d(tag, "The thread was interrupted")
                    interrupt()
                    break
                }
                catch (exception: Exception) {
                    Log.d(tag, "Failed to update the debugging status.")
                    Log.d(tag, exception.stackTraceToString())
                }
            }
        }
    }

}
