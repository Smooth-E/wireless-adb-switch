package com.smoothie.wirelessDebuggingSwitch

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


abstract class WidgetConfigurationActivity(preferenceScreen: Int) : CollapsingToolbarActivity(
    WidgetConfigurationFragment(preferenceScreen),
    R.string.header_configure_widget
) {

    companion object {
        private const val TAG = "WidgetConfigurationActivity"
    }

    override fun onStop() {
        super.onStop()

        val widgetId =
            intent?.extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID) ?: INVALID_APPWIDGET_ID

        if (widgetId == INVALID_APPWIDGET_ID) {
            Log.d(TAG, "Widget ID was invalid!")
            return
        }

        val updateIntent = Intent()
            .setAction(ACTION_APPWIDGET_UPDATE)
            .putExtra(EXTRA_APPWIDGET_ID, widgetId)
        sendBroadcast(updateIntent)

        val intent = Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, intent)
    }

    protected abstract fun generateWidget(preferences: SharedPreferences) : View

    class OnWidgetConfigurationChangeListener(
        private val widgetConfigurationActivity: WidgetConfigurationActivity,
        private val previewView: ViewGroup
    ) : OnSharedPreferenceChangeListener {

        companion object {
            private const val TAG = "OnWidgetConfigurationChangeListener"
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            if (sharedPreferences == null) {
                Log.d(TAG, "No configuration was given to draw widget preview!")
                return
            }

            val view = widgetConfigurationActivity.generateWidget(sharedPreferences)
            previewView.removeAllViews()
            previewView.addView(view)
        }
    }

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
        }

        override fun onDestroy() {
            super.onDestroy()

            preferenceManager
                .sharedPreferences?.unregisterOnSharedPreferenceChangeListener(listener)
        }

    }

    class WidgetConfigurationFragment(
        private val preferenceScreen: Int
    ) : Fragment(R.layout.fragment_widget_preferences) {

        private lateinit var activity: WidgetConfigurationActivity
        private var widgetId: Int = INVALID_APPWIDGET_ID

        private lateinit var widgetConfigurationChangeListener: OnSharedPreferenceChangeListener

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            activity = requireActivity() as WidgetConfigurationActivity
            activity.setResult(RESULT_CANCELED)

            fun setWallpaper() {
                val wallpaperManager = WallpaperManager.getInstance(activity)
                view.findViewById<ShapeableImageView>(R.id.showcase_background)
                    .setImageDrawable(wallpaperManager.drawable)
            }

            val permissionLauncher = registerForActivityResult(RequestPermission()) { granted ->
                if (granted)
                    setWallpaper()
            }

            val permissionState = ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE)
            if (permissionState == PERMISSION_GRANTED)
                setWallpaper()
            else
                permissionLauncher.launch(READ_EXTERNAL_STORAGE)

            val extras = activity.intent.extras
            widgetId =
                extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID ) ?: INVALID_APPWIDGET_ID

            if (widgetId == INVALID_APPWIDGET_ID) {
                activity.finish()
                return
            }

            val previewViewGroup = view.findViewById<ViewGroup>(R.id.preview_holder)
            widgetConfigurationChangeListener =
                OnWidgetConfigurationChangeListener(activity, previewViewGroup)

            val preferencesName = Utilities.getWidgetSharedPreferencesName(widgetId)
            val preferenceFragment = WidgetConfigurationPreferenceFragment(
                preferenceScreen,
                preferencesName,
                widgetConfigurationChangeListener
            )

            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.preference_fragment_holder, preferenceFragment)
                .commit()
        }

    }

}
