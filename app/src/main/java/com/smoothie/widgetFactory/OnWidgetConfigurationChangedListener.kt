package com.smoothie.widgetFactory

import android.content.SharedPreferences
import android.util.Log
import android.view.ViewGroup
import android.view.ViewTreeObserver

class OnWidgetConfigurationChangedListener(
    private val activity: WidgetConfigurationActivity,
    private val previewView: ViewGroup
) : SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "OnWidgetConfigurationChangeListener"
    }

    private inner class GlobalLayoutListener(
        private val sharedPreferences: SharedPreferences
    ) : ViewTreeObserver.OnGlobalLayoutListener {

        override fun onGlobalLayout() {
            updatePreview(sharedPreferences, previewView)
            previewView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }

    }

    private fun updatePreview(widgetSharedPreferences: SharedPreferences, previewView: ViewGroup) {

        val previewHeight =
            previewView.height - previewView.paddingBottom - previewView.paddingTop
        val width = (previewHeight * activity.previewAspectRatio).toInt()
        val height = (previewHeight / activity.previewAspectRatio).toInt()

        val view = activity.generateWidget(width, height, widgetSharedPreferences)
        view.layoutParams = ViewGroup.LayoutParams(width, height)

        previewView.removeAllViews()
        previewView.addView(view)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        if (sharedPreferences == null) {
            Log.d(TAG, "No configuration was given to draw widget preview!")
            return
        }

        if (!previewView.isAttachedToWindow) {
            previewView.viewTreeObserver
                .addOnGlobalLayoutListener(GlobalLayoutListener(sharedPreferences))
        }
        else
            updatePreview(sharedPreferences, previewView)
    }
}
