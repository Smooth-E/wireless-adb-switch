package com.smoothie.wirelessDebuggingSwitch.widget

import android.appwidget.AppWidgetManager
import android.content.*
import android.util.Log
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.wirelessDebuggingSwitch.WirelessDebugging

abstract class SwitchWidget(private val className: String) : ConfigurableWidget(className) {

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

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive() Action: ${intent?.action}")
        if (intent?.hasExtra(INTENT_FLAG_CHANGE_STATE) != true) {
            Log.d(TAG, "Calling super! ${intent?.action}")
            super.onReceive(context, intent)
            return
        }

        switchState = SwitchState.Waiting
        updateAllWidgets(context!!)

        WirelessDebugging.setEnabled(context, !WirelessDebugging.getEnabled(context))
        switchState =
            if (WirelessDebugging.getEnabled(context))
                SwitchState.Enabled
            else
                SwitchState.Disabled

        updateAllWidgets(context)

        if (switchState == SwitchState.Enabled)
            WirelessDebugging.syncConnectionData(context)
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        switchState =
            if (WirelessDebugging.getEnabled(context!!))
                SwitchState.Enabled
            else
                SwitchState.Disabled
    }

    protected fun createStateSwitchIntent(context: Context): Intent {
        val componentName = ComponentName(context.applicationContext, className)
        val manager = AppWidgetManager.getInstance(context)
        val widgetIds = manager.getAppWidgetIds(componentName)

        val intent = Intent()
        intent.component = componentName
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        intent.putExtra(INTENT_FLAG_CHANGE_STATE, true)

        return intent
    }

}
