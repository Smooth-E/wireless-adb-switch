package com.smoothie.widgetFactory

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
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
            window.navigationBarColor = getColor(R.color.colorOnSecondary)
            window.navigationBarDividerColor = getColor(R.color.colorOutline)
        }
    }

}