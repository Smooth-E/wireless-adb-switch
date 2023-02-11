package com.smoothie.widgetFactory.configuration

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.content.DialogInterface
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresPermission
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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



        val runningTiramisu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val needsDifferentPermission = runningTiramisu && !Environment.isExternalStorageManager()

        Log.i(TAG, "Running Tiramisu and higher: $runningTiramisu")
        Log.i(TAG, "Needs different permission: $needsDifferentPermission")

        val preferenceKey = getString(R.string.key_ignore_optional_wallpaper_permission)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val ignoreDifferentPermission = preferences.getBoolean(preferenceKey, false)

        if (needsDifferentPermission && !ignoreDifferentPermission) {
            val listener = DialogInterface.OnClickListener { dialog, button ->
                when (button) {
                    DialogInterface.BUTTON_POSITIVE -> requestManageAllFilesPermission()
                    DialogInterface.BUTTON_NEUTRAL -> {
                        preferences.edit().putBoolean(preferenceKey, true).apply()
                        dialog.dismiss()
                    }
                }
            }

            MaterialAlertDialogBuilder(this)
                .setPositiveButton(R.string.action_grant_permission, listener)
                .setNeutralButton(R.string.action_dismiss, listener)
                .setTitle(R.string.header_optional_permission)
                .setMessage(R.string.message_allow_all_files_for_wallpaper)
                .setCancelable(false)
                .show()
        }

        val storagePermissionLauncher = registerForActivityResult(RequestPermission()) { granted ->
            if (granted)
                setWallpaper()
            else
                Log.w(TAG, "Permission needed to get wallpaper not granted!")
        }

        val storagePermissionState =
            ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)

        if (needsDifferentPermission)
            return

        if (!runningTiramisu && storagePermissionState != PERMISSION_GRANTED)
            storagePermissionLauncher.launch(READ_EXTERNAL_STORAGE)
        else
            setWallpaper()
    }

    override fun onResume() {
        super.onResume()

        val readExternalStoragePermissionState =
            ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)

        val permissionGranted =
            readExternalStoragePermissionState == PERMISSION_GRANTED ||
            Environment.isExternalStorageManager()

        if (permissionGranted)
            setWallpaper()
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

    private fun requestManageAllFilesPermission() {
        val uri = Uri.fromParts("package", packageName, null)
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = uri
        startActivity(intent)
    }

    @RequiresPermission(anyOf = [READ_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE])
    private fun setWallpaper() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val drawable = wallpaperManager.drawable ?: return
        findViewById<ShapeableImageView>(R.id.showcase_background).setImageDrawable(drawable)
    }

    @SuppressLint("InflateParams")
    abstract fun generateWidget(
        width: Int,
        height: Int,
        widgetPreferences: SharedPreferences
    ) : View

}
