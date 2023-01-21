package com.smoothie.wirelessDebuggingSwitch

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.imageview.ShapeableImageView


open class WidgetConfigurationActivity(preferenceScreen: Int) : CollapsingToolbarActivity(
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

    class WidgetConfigurationPreferenceFragment(
        private val preferenceResource: Int,
        private val sharedPreferencesName: String
    ) : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = sharedPreferencesName
            setPreferencesFromResource(preferenceResource, rootKey)
        }

    }

    class WidgetConfigurationFragment(
        private val preferenceScreen: Int
    ) : Fragment(R.layout.fragment_widget_preferences) {

        private lateinit var activity: WidgetConfigurationActivity
        private var widgetId: Int = INVALID_APPWIDGET_ID

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

            val preferencesName = Utilities.getWidgetSharedPreferencesName(widgetId)
            val preferenceFragment =
                WidgetConfigurationPreferenceFragment(preferenceScreen, preferencesName)

            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.preference_fragment_holder, preferenceFragment)
                .commit()
        }

    }

}
