package com.smoothie.wirelessDebuggingSwitch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.smoothie.wirelessDebuggingSwitch.view.CollapsingTopBar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val topBar = findViewById<CollapsingTopBar>(R.id.top_bar)
        topBar.primaryOnClickListener = null
        topBar.secondaryOnClickListener = null
        topBar.attachToScrollView(findViewById(R.id.scroll_view))

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preference_fragment, PreferencesManagementFragment())
            .commit()
    }
}
