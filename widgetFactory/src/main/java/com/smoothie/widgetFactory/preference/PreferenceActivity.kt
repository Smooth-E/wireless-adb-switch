package com.smoothie.widgetFactory.preference

import com.smoothie.widgetFactory.CollapsingToolbarActivity
import com.smoothie.widgetFactory.R

open class PreferenceActivity(
    val preferencesResourceId: Int,
    titleStringResource: Int = R.string.header_settings
) : CollapsingToolbarActivity(
    titleStringResource,
    PreferenceFragment()
)
