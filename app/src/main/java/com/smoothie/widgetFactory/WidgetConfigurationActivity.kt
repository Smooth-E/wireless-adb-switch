package com.smoothie.widgetFactory

import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment

abstract class WidgetConfigurationActivity(
    private val preferenceScreen: Int,
    val previewAspectRatio: Float
) : OrientationDependantActivity() {

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

    override fun createLandscapeFragment(): Fragment =
        WidgetConfigurationFragment(preferenceScreen, true)

    override fun createPortraitFragment(): Fragment =
        WidgetConfigurationFragment(preferenceScreen, false)

    abstract fun generateWidget(
        width: Int,
        height: Int,
        widgetPreferences: SharedPreferences
    ) : View

}
