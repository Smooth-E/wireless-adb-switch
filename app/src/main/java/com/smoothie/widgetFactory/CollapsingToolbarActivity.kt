package com.smoothie.widgetFactory

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.smoothie.wirelessDebuggingSwitch.R

open class CollapsingToolbarActivity(
    private val titleStringResource: Int,
    private val contentFragment: Fragment
) : FullScreenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collapsing_toolbar)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = getString(titleStringResource)
        toolbar.setOnClickListener { finish() }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_holder, contentFragment)
            .commit()
    }

}
