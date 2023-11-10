package com.smoothie.widgetFactory

import android.os.Bundle
import com.smoothie.widgetFactory.preference.PreferenceActivity

/**
 * /this derive]atu=ive of [PreferenceActivity] will hide the navigation up button from the toolbar
 * if the [EXTRA_KEEP_NAVIGATION_UP] is not found inside of an intent.
 */
open class ApplicationPreferenceActivity(
    preferenceResourceId: Int = R.xml.preferences_app,
    titleStringResource: Int = R.string.settings
) : PreferenceActivity(preferenceResourceId, titleStringResource) {

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

}
