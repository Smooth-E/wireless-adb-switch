package com.smoothie.wirelessDebuggingSwitch.widget

import android.appwidget.AppWidgetManager
import android.content.*
import android.util.Log
import android.widget.Toast
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.wirelessDebuggingSwitch.WirelessDebugging
import com.topjohnwu.superuser.Shell

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

            WirelessDebugging.enabled = !WirelessDebugging.enabled
            switchState = if (WirelessDebugging.enabled)
                SwitchState.Enabled
            else
                SwitchState.Disabled

            updateAllWidgets(context)

            if (switchState == SwitchState.Enabled) {
                val connectionAddress: String
                try {
                    val port = WirelessDebugging.getPort()
                    val address = WirelessDebugging.getAddress(context)
                    connectionAddress = "$address:$port"
                }
                catch (exception: Exception) {
                    Log.e(TAG, "Unable to get connection address and port.")
                    exception.printStackTrace()
                    return
                }

                val label = "Wireless debugging connection address"
                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText(label, connectionAddress))

                val packageName = "org.kde.kdeconnect_tp"
                val activityName =
                    "org.kde.kdeconnect.Plugins.ClibpoardPlugin.ClipboardFloatingActivity"
                val command =
                    "am start " +
                    "-n $packageName/$activityName " +
                    "--ez SHOW_TOAST 1"

                val result = Shell.cmd(command).exec()
                Log.d(TAG, result.toString())
            }
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
