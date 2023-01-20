package com.smoothie.wirelessDebuggingSwitch

import android.app.Application
import android.util.Log

class CustomApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("Application", "Application created!")

        WidgetUpdater.enable(this)
    }

}
