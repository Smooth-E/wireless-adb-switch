package com.smoothie.wirelessDebuggingSwitch

import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat

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
