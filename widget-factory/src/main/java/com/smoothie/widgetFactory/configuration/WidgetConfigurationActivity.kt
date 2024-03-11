package com.smoothie.widgetFactory.configuration

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.smoothie.widgetFactory.ApplicationPreferenceActivity
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.widgetFactory.configuration.PreferenceFragment.Companion.KEY_PREFERENCES_NAME
import com.smoothie.widgetFactory.configuration.PreferenceFragment.Companion.KEY_PREFERENCES_RESOURCE
import com.smoothie.widgetFactory.configuration.PreferenceFragment.Companion.KEY_PREVIEW_ASPECT
import com.smoothie.widgetFactory.FullScreenActivity
import com.smoothie.widgetFactory.R
import com.smoothie.widgetFactory.WidgetCutoutDrawable

abstract class WidgetConfigurationActivity(
    private val preferencesResourceId: Int,
    private val previewAspectRatio: Float
) : FullScreenActivity() {

    companion object {
        private const val TAG = "WidgetConfigurationActivity"
    }

    private var widgetId: Int = INVALID_APPWIDGET_ID

    private val settingsToolbarItemClickListener = Toolbar.OnMenuItemClickListener { menuItem ->
        if (menuItem.itemId != R.id.settings)
            return@OnMenuItemClickListener false

        val intent = packageManager.getLaunchIntentForPackage(application.packageName)

        if (intent == null) {
            Log.e(TAG, "No activity found to handle a launch intent!")
            return@OnMenuItemClickListener false
        }

        intent.action = Intent.ACTION_APPLICATION_PREFERENCES
        intent.putExtra(ApplicationPreferenceActivity.EXTRA_KEEP_NAVIGATION_UP, true)
        startActivity(intent)
        return@OnMenuItemClickListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setDimAmount(0f)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)

        setContentView(R.layout.activity_widget_configuration)

        setResult(RESULT_CANCELED)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.widget_configuration_toolbar)
        toolbar.setOnMenuItemClickListener(settingsToolbarItemClickListener)

        val dimension = resources.displayMetrics.density * 16
        val colorSurface = resources.getColor(R.color.colorSurface, theme)
        val cutoutDrawable = WidgetCutoutDrawable(dimension, dimension, colorSurface)
        findViewById<ImageView>(R.id.showcase_background).setImageDrawable(cutoutDrawable)

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

    @SuppressLint("InflateParams")
    abstract fun generateWidget(
        width: Int,
        height: Int,
        widgetPreferences: SharedPreferences
    ) : View

}
