package com.smoothie.widgetFactory

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.imageview.ShapeableImageView
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.Utilities

abstract class WidgetConfigurationActivity(
    private val preferenceScreen: Int,
    val previewAspectRatio: Float
) : CollapsingToolbarActivity(
    WidgetConfigurationFragment(preferenceScreen),
    R.string.header_configure_widget
) {

    companion object {
        private const val TAG = "WidgetConfigurationActivity"
    }

    override fun finish() {
        val widgetId =
            intent?.extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID) ?: INVALID_APPWIDGET_ID

        if (widgetId == INVALID_APPWIDGET_ID) {
            Log.d(TAG, "Widget ID was invalid!")
            super.finish()
        }

        val updateIntent = Intent()
            .setAction(ACTION_APPWIDGET_UPDATE)
            .putExtra(EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        sendBroadcast(updateIntent)

        val intent = Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, intent)

        super.finish()
    }

    abstract fun generateWidget(
        width: Int,
        height: Int,
        preferences: SharedPreferences
    ) : View

    class WidgetConfigurationPreferenceFragment(
        private val preferenceResource: Int,
        private val sharedPreferencesName: String,
        private val listener: OnSharedPreferenceChangeListener
    ) : PreferenceFragmentCompat() {

        companion object {
            private const val TAG = "WidgetConfigurationPreferenceFragment"
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = sharedPreferencesName
            setPreferencesFromResource(preferenceResource, rootKey)

            val preferences = preferenceManager.sharedPreferences

            if (preferences == null) {
                Log.d(TAG, "preferenceManager.sharedPreferences is null!")
                requireActivity().finish()
                return
            }

            preferences.registerOnSharedPreferenceChangeListener(listener)

            // This will generate widget preview on activity startup
            listener.onSharedPreferenceChanged(preferences, rootKey)
        }

        override fun onDestroy() {
            super.onDestroy()

            preferenceManager
                .sharedPreferences?.unregisterOnSharedPreferenceChangeListener(listener)
        }

    }

}
