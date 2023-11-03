package com.smoothie.widgetFactory.preference

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import com.smoothie.widgetFactory.CollapsingToolbarActivity
import com.smoothie.widgetFactory.R

open class PreferenceActivity(
    val preferencesResourceId: Int,
    titleStringResource: Int = R.string.settings
) : CollapsingToolbarActivity(
    titleStringResource,
    PreferenceFragment()
) {

    companion object {
        const val EXTRA_KEEP_NAVIGATION_UP = "com.smoothie.widgetFactory.KEEP_NAVIGATION_UP"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!intent.hasExtra(EXTRA_KEEP_NAVIGATION_UP)) {
            toolbar.setNavigationOnClickListener(null)
            toolbar.navigationIcon = null
        }
    }

    @CallSuper
    open fun onPreferencesCreated(preferenceFragment: PreferenceFragment) {  }

    @CallSuper
    open fun onPreferenceFragmentViewCreated(view: View, savedInstanceState: Bundle?) { }

}
