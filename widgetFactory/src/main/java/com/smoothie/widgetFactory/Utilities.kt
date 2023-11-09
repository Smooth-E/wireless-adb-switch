package com.smoothie.widgetFactory

import android.graphics.Insets
import android.view.View
import android.view.WindowInsets

fun getInsets(
    view: View,
    typeMask: Int = WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
): Insets =
    view.rootWindowInsets.getInsetsIgnoringVisibility(typeMask)
