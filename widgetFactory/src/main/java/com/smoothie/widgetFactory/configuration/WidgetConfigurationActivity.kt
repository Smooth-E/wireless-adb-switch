package com.smoothie.widgetFactory.configuration

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.imageview.ShapeableImageView
import com.smoothie.widgetFactory.FullScreenActivity
import com.smoothie.widgetFactory.R
import com.smoothie.widgetFactory.configuration.PreferenceFragment.Companion.KEY_PREFERENCES_NAME
import com.smoothie.widgetFactory.configuration.PreferenceFragment.Companion.KEY_PREFERENCES_RESOURCE
import com.smoothie.widgetFactory.configuration.PreferenceFragment.Companion.KEY_PREVIEW_ASPECT

abstract class WidgetConfigurationActivity(
    private val preferencesResourceId: Int,
    private val previewAspectRatio: Float
) : FullScreenActivity() {

    companion object {
        private const val TAG = "WidgetConfigurationActivity"
    }

    private var widgetId: Int = INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_configuration)

        setResult(RESULT_CANCELED)

        findViewById<Toolbar>(R.id.toolbar).setOnClickListener { finish() }

        val permissionLauncher = registerForActivityResult(RequestPermission()) { granted ->
            if (granted)
                setWallpaper()
        }

        val permissionState = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permissionState == PackageManager.PERMISSION_GRANTED)
            setWallpaper()
        else
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        val extras = intent.extras
        widgetId = extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID) ?: INVALID_APPWIDGET_ID

        if (widgetId == INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val preferencesName = WidgetPreferences.getWidgetSharedPreferencesName(widgetId)

        val arguments = Bundle()
        arguments.putString(KEY_PREFERENCES_NAME, preferencesName)
        arguments.putInt(KEY_PREFERENCES_RESOURCE, preferencesResourceId)
        arguments.putFloat(KEY_PREVIEW_ASPECT, previewAspectRatio)

        val fragmentClass =  PreferenceFragment::class.java

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preference_fragment_holder, fragmentClass, arguments)
            .commit()
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

    @SuppressLint("MissingPermission")
    private fun setWallpaper() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        findViewById<ShapeableImageView>(R.id.showcase_background)
            .setImageDrawable(wallpaperManager.drawable)
    }

    abstract fun generateWidget(
        width: Int,
        height: Int,
        widgetPreferences: SharedPreferences
    ) : View

}
