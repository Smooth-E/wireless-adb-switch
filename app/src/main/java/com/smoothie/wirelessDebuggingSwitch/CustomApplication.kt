package com.smoothie.wirelessDebuggingSwitch

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.smoothie.widgetFactory.ConfigurableWidget
import com.smoothie.widgetFactory.WidgetFactoryApplication
import com.smoothie.wirelessDebuggingSwitch.widget.BasicWidget
import com.smoothie.wirelessDebuggingSwitch.widget.InformationWidget
import com.topjohnwu.superuser.Shell
import rikka.shizuku.Shizuku

class CustomApplication : WidgetFactoryApplication() {

    companion object {
        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG

            val builder = Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
            Shell.setDefaultBuilder(builder)
        }
    }

    override fun onCreate() {
        Shell.getShell()
        if (Shell.isAppGrantedRoot() != true) {
            if (!ShizukuUtilities.isShizukuAvailable()) {
                val message = getString(R.string.message_no_way_to_work)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                return
            }

            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                ShizukuUtilities.requestShizukuPermission { isGranted ->
                    if (!isGranted) {
                        Log.d("ShizukuPermission", "Permission is not granted!")
                        val message = getString(R.string.message_need_at_least_shizuku)
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                        val intent = Intent(this, SettingsActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra("EXIT", true)
                        startActivity(intent)

                        return@requestShizukuPermission
                    }

                    // super.onCreate()
                    addWidgets()
                }
            }
        }

        super.onCreate()
        addWidgets()
    }

    private fun addWidgets() {
        ConfigurableWidget.addWidget(BasicWidget::class.java.name)
        ConfigurableWidget.addWidget(InformationWidget::class.java.name)
    }

}
