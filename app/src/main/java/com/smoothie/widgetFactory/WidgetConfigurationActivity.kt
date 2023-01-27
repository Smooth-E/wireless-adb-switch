package com.smoothie.widgetFactory

import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.smoothie.wirelessDebuggingSwitch.R

abstract class WidgetConfigurationActivity(
    val preferencesResourceId: Int,
    val previewAspectRatio: Float
) : CollapsingToolbarActivity(R.string.header_configure_widget, WidgetConfigurationFragment()) {

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
        widgetPreferences: SharedPreferences
    ) : View

}
