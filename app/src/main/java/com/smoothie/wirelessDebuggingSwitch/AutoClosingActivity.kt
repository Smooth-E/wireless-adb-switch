package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

/**
 * This activity automatically finishes itself in onCreate.
 * Use the [start] method to safely finish the app without killing the process directly.
 */
class AutoClosingActivity : AppCompatActivity() {

    companion object {
        fun start(from: Context) {
            val intent = Intent(from, AutoClosingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            from.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
