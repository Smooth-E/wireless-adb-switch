package com.smoothie.wirelessDebuggingSwitch

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class CustomApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val workRequest =
            PeriodicWorkRequestBuilder<WidgetUpdateWorker>(1, TimeUnit.SECONDS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WidgetUpdateWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

}
