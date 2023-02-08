package com.smoothie.widgetFactory

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

open class FullScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT

        window.attributes.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        val transparentNavigationBar =
            NavigationBar.getInteractionMode(this) == NavigationBar.MODE_GESTURES ||
            resources.configuration.orientation == ORIENTATION_LANDSCAPE

        if (transparentNavigationBar) {
            window.navigationBarColor = Color.TRANSPARENT
            window.navigationBarDividerColor = Color.TRANSPARENT
            return
        }

        val id = com.google.android.material.R.attr.colorSurfaceInverse
        val typedValue = TypedValue()
        theme.resolveAttribute(id, typedValue, true)
        val outlineColor = typedValue.data

        window.navigationBarDividerColor = outlineColor
        window.navigationBarColor = getColor(R.color.colorSurface)
    }

}
