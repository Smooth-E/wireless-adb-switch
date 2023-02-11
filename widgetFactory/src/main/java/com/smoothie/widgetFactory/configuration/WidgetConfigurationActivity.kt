package com.smoothie.widgetFactory.configuration

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.CallSuper
import androidx.annotation.RequiresPermission
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.imageview.ShapeableImageView
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.widgetFactory.configuration.PreferenceFragment.Companion.KEY_PREFERENCES_NAME
import com.smoothie.widgetFactory.configuration.PreferenceFragment.Companion.KEY_PREFERENCES_RESOURCE
import com.smoothie.widgetFactory.configuration.PreferenceFragment.Companion.KEY_PREVIEW_ASPECT
import com.smoothie.widgetFactory.FullScreenActivity
import com.smoothie.widgetFactory.R

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

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.widget_configuration_toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId != R.id.settings)
                return@setOnMenuItemClickListener false

            val intent = packageManager.getLaunchIntentForPackage(application.packageName)

            if (intent == null) {
                Log.e(TAG, "No activity found to handle a launch intent!")
                return@setOnMenuItemClickListener false
            }

            intent.action = Intent.ACTION_APPLICATION_PREFERENCES
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }

        val permissionLauncher = registerForActivityResult(RequestPermission()) { granted ->
            if (granted)
                setWallpaper(getWallpaperDrawable())
            else
                Log.w(TAG, "Permission needed to get wallpaper not granted!")
        }

        val permissionState = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("", "Requesting wallpaper for newer SDK!")
            setWallpaper(getWallpaperOnNewerSdk())
        }
        else if (permissionState == PackageManager.PERMISSION_GRANTED)
            setWallpaper(getWallpaperDrawable())
        else
            permissionLauncher.launch(READ_EXTERNAL_STORAGE)

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
            return
        }

        // TODO: Figure out how to update only one widget instead of all available
        // TODO: Sending an ACTION_APPWIDGET_UPDATE intent does nothing for some reason
        ConfigurableWidget.updateAllWidgets(this)

        val intent = Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, intent)

        super.finish()
    }

    private fun setWallpaper(drawable: Drawable?) {
        if (drawable == null)
            return

        findViewById<ShapeableImageView>(R.id.showcase_background).setImageDrawable(drawable)
    }

    @RequiresPermission(READ_EXTERNAL_STORAGE)
    private fun getWallpaperDrawable(): Drawable? {
        val wallpaperManager = WallpaperManager.getInstance(this)
        return wallpaperManager.drawable
    }

    /**
     * As of Android 13 (API 33, Tiramisu) READ_EXTERNAL_STORAGE permission which is needed to get
     * the wallpaper with a WallpaperManager is no longer available.
     *
     * It is still possible to get wallpaper if you have root access, for example.
     *
     * Apps using this library should overwrite this method if they need
     * to get wallpaper on Android 13+
     *
     * @return retrieved wallpaper drawable
     */
    abstract fun getWallpaperOnNewerSdk(): Drawable?

    @SuppressLint("InflateParams")
    abstract fun generateWidget(
        width: Int,
        height: Int,
        widgetPreferences: SharedPreferences
    ) : View

}
