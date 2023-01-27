package com.smoothie.widgetFactory

import android.Manifest
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.imageview.ShapeableImageView
import com.smoothie.wirelessDebuggingSwitch.R

class WidgetConfigurationFragment : Fragment(R.layout.fragment_widget_preferences) {

    private var preferenceScreen: Int = -1
    private lateinit var activity: WidgetConfigurationActivity
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = requireActivity() as WidgetConfigurationActivity

        preferenceScreen = activity.preferencesResourceId

        activity.setResult(AppCompatActivity.RESULT_CANCELED)

        fun setWallpaper() {
            val wallpaperManager = WallpaperManager.getInstance(activity)
            view.findViewById<ShapeableImageView>(R.id.showcase_background)
                .setImageDrawable(wallpaperManager.drawable)
        }

        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
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

        val preferencesName = WidgetConfiguration.getWidgetSharedPreferencesName(widgetId)

        val arguments = Bundle()
        arguments.putString(
            WidgetConfigurationPreferenceFragment.KEY_PREFERENCES_NAME,
            preferencesName
        )
        arguments.putInt(
            WidgetConfigurationPreferenceFragment.KEY_PREFERENCES_RESOURCE,
            preferenceScreen
        )

        val fragmentClass =  WidgetConfigurationPreferenceFragment::class.java

        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.preference_fragment_holder, fragmentClass, arguments)
            .commit()
    }

}
