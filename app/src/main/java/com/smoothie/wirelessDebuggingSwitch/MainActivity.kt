package com.smoothie.wirelessDebuggingSwitch

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.elevation.SurfaceColors

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT

        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).contentScrim =
            ColorDrawable(SurfaceColors.getColorForElevation(this, 8f))

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnClickListener { finish() }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preference_fragment, PreferencesManagementFragment())
            .commit()
    }
}
