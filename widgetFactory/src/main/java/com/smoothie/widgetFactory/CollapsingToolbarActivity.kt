package com.smoothie.widgetFactory

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.doOnAttach
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar

open class CollapsingToolbarActivity(
    private val titleStringResource: Int,
    private val contentFragment: Fragment,
    private val addToolbarNavigation: Boolean = true
) : FullScreenActivity() {

    lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collapsing_toolbar)

        toolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(titleStringResource)

        if (addToolbarNavigation)
            toolbar.setNavigationOnClickListener { finish() }
        else
            toolbar.navigationIcon = null

        fixSystemWindowsPadding()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_holder, contentFragment)
            .commit()
    }

    private fun fixSystemWindowsPadding() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE)
            return

        val toolbarFrame = findViewById<ViewGroup>(R.id.toolbar_frame) ?: return
        val fragmentHolder = findViewById<ViewGroup>(R.id.fragment_holder) ?: return

        toolbarFrame.fitsSystemWindows = false
        fragmentHolder.fitsSystemWindows = false

        window.decorView.doOnAttach { decorView ->
            val typeMask = WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            val insets = decorView.rootWindowInsets.getInsetsIgnoringVisibility(typeMask)

            toolbarFrame.setPadding(
                toolbarFrame.paddingLeft + insets.left,
                toolbarFrame.paddingTop + insets.top,
                toolbarFrame.paddingRight + insets.right,
                toolbarFrame.paddingBottom
            )

            fragmentHolder.setPadding(
                fragmentHolder.paddingLeft + insets.left,
                fragmentHolder.paddingTop,
                fragmentHolder.paddingRight + insets.right,
                fragmentHolder.paddingBottom
            )
        }
    }

}
