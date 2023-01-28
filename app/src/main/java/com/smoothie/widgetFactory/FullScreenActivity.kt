package com.smoothie.widgetFactory

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.smoothie.wirelessDebuggingSwitch.R

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
            val outlineColorId = if (resources.configuration.orientation == ORIENTATION_PORTRAIT)
                R.color.colorSurfaceInverse
            else
                R.color.colorTransparent

            window.navigationBarColor = getColor(R.color.colorSurface)
            window.navigationBarDividerColor = getColor(outlineColorId)
        }

        window.attributes.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

}