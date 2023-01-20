package com.smoothie.wirelessDebuggingSwitch

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(applicationContext: Context, workerParameters: WorkerParameters) :
    Worker(applicationContext, workerParameters) {

    companion object {
        const val UNIQUE_WORK_NAME = "com.smoothie.wirelessDebuggingSwitch.WidgetUpdateWork"
    }

    override fun doWork(): Result {
        val widgetIds = AbstractSwitchWidget.getAllWidgetIds(applicationContext)
        val intent = Intent()
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        intent.putExtra(AbstractSwitchWidget.INTENT_EXTRA_FLAG_NAME, true)
        applicationContext.sendBroadcast(intent)

        Log.d("WidgetUpdateWorker", "Work done, intent sent.")

        return Result.success()
    }

}
