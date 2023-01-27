package com.smoothie.widgetFactory

import android.Manifest
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.imageview.ShapeableImageView
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.Utilities

class WidgetConfigurationFragment(
    private val preferenceScreen: Int,
    isPortrait: Boolean
) : Fragment(
    if (isPortrait)
        R.layout.fragment_widget_preferences
    else
        R.layout.fragment_widget_preferences
) {

    private lateinit var activity: WidgetConfigurationActivity
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var widgetConfigurationChangeListener: OnSharedPreferenceChangeListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = requireActivity() as WidgetConfigurationActivity
        activity.setResult(AppCompatActivity.RESULT_CANCELED)

        fun setWallpaper() {
            val wallpaperManager = WallpaperManager.getInstance(activity)
            view.findViewById<ShapeableImageView>(R.id.showcase_background)
                .setImageDrawable(wallpaperManager.drawable)
        }

        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted)
                    setWallpaper()
        }

        val permissionState = ContextCompat.checkSelfPermission(activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permissionState == PackageManager.PERMISSION_GRANTED)
            setWallpaper()
        else
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        val extras = activity.intent.extras
        widgetId =
            extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            activity.finish()
            return
        }

        val previewViewGroup = view.findViewById<ViewGroup>(R.id.preview_holder)
        widgetConfigurationChangeListener =
            OnWidgetConfigurationChangedListener(activity, previewViewGroup)

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
