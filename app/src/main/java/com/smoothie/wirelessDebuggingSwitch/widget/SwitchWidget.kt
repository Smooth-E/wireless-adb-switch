package com.smoothie.wirelessDebuggingSwitch.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.wirelessDebuggingSwitch.WirelessDebugging

abstract class SwitchWidget(name: String) : ConfigurableWidget(name) {

    enum class SwitchState {
        Disabled,
        Waiting,
        Enabled
    }

    companion object {

        private const val TAG = "SwitchWidget"

        const val INTENT_FLAG_CHANGE_STATE =
            "com.smoothie.wirelessDebuggingSwitch.intent.FLAG_CHANGE_STATE"

        @JvmStatic
        protected var switchState: SwitchState = SwitchState.Disabled
            private set

        @JvmStatic
        protected fun createStateSwitchIntent(context: Context): Intent {
            val intent = createBasicIntent(context)
            intent.putExtra(INTENT_FLAG_CHANGE_STATE, true)
            return intent
        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.hasExtra(INTENT_FLAG_CHANGE_STATE) == true) {
            switchState = SwitchState.Waiting
            updateAllWidgets(context!!)

            Log.d(TAG, "Wireless debugging switching begun!")

            WirelessDebugging.enabled = !WirelessDebugging.enabled
            switchState = if (WirelessDebugging.enabled)
                SwitchState.Enabled
            else
                SwitchState.Disabled

            updateAllWidgets(context)

            Log.d(TAG, "Wireless debugging switching finished!")
        }
        else
            super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        switchState = if (WirelessDebugging.enabled) SwitchState.Enabled else SwitchState.Disabled
    }

}
