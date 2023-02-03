package com.smoothie.wirelessDebuggingSwitch

import android.content.IntentFilter
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.widgetFactory.WidgetFactoryApplication
import com.smoothie.wirelessDebuggingSwitch.widget.basic.BasicWidget
import com.smoothie.wirelessDebuggingSwitch.widget.coupled.CoupledWidget
import com.topjohnwu.superuser.Shell

class CustomApplication : WidgetFactoryApplication() {

    companion object {

        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG;

            val builder = Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)

            Shell.setDefaultBuilder(builder);
        }

    }

    override fun onCreate() {
        super.onCreate()

        ConfigurableWidget.addWidget(BasicWidget::class.java.name)
        ConfigurableWidget.addWidget(CoupledWidget::class.java.name)

        Shell.getShell()
    }

}
