package com.smoothie.widgetFactory.configuration

import android.content.SharedPreferences
import android.util.Log
import android.view.ViewGroup
import android.view.ViewTreeObserver

class OnConfigurationChangedListener(
    private val activity: WidgetConfigurationActivity,
    private val previewView: ViewGroup,
    private val previewAspectRatio: Float
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
        Log.d(TAG, "Updating preview!")

        val previewHeight =
            previewView.height - previewView.paddingBottom - previewView.paddingTop
        val width = (previewHeight * previewAspectRatio).toInt()
        val height = (previewHeight / previewAspectRatio).toInt()

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
