package com.smoothie.wirelessDebuggingSwitch

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.widgetFactory.WidgetFactoryApplication
import com.smoothie.wirelessDebuggingSwitch.widget.BasicWidget
import com.smoothie.wirelessDebuggingSwitch.widget.InformationWidget
import com.topjohnwu.superuser.Shell

class CustomApplication : WidgetFactoryApplication() {

    companion object {

        const val PRIVILEGE_NOTIFICATION_CHANNEL_ID =
            "com.smoothie.wadbs.missing_privileges_channel"
        const val PRIVILEGE_NOTIFICATION_ID = 0
        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG

            val builder = Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
            Shell.setDefaultBuilder(builder)
        }

    }

    override fun onCreate() {
        super.onCreate()
        addWidgets()
        Shell.getShell()
        createMissingPrivilegeNotificationsChannel()
    }

    private fun addWidgets() {
        ConfigurableWidget.addWidget(BasicWidget::class.java.name)
        ConfigurableWidget.addWidget(InformationWidget::class.java.name)
    }

    private fun createMissingPrivilegeNotificationsChannel() {
        val channel = NotificationChannel(
            PRIVILEGE_NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_title),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = getString(R.string.notification_channel_description)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannel(channel)

        if (hasSufficientPrivileges())
            notificationManager.cancel(PRIVILEGE_NOTIFICATION_ID)
    }

}
