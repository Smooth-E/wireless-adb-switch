package com.smoothie.widgetFactory

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

open class FullScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.isNavigationBarContrastEnforced = false

        if (NavigationBar.getInteractionMode(this) == NavigationBar.MODE_GESTURES) {
            window.navigationBarColor = Color.TRANSPARENT
            window.navigationBarDividerColor = Color.TRANSPARENT
        }
        else {
            val outlineColor = if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
                val id = com.google.android.material.R.attr.colorSurfaceInverse
                val typedValue = TypedValue()
                theme.resolveAttribute(id, typedValue, true)
                typedValue.data
            }
            else
                Color.TRANSPARENT
            window.navigationBarDividerColor = outlineColor

            val typedValue = TypedValue()
            val resourceId = com.google.android.material.R.attr.colorSurface
            theme.resolveAttribute(resourceId, typedValue, true)
            window.navigationBarColor = getColor(typedValue.data)
        }

        window.attributes.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

}
