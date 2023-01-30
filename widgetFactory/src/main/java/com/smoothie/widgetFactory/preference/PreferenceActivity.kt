package com.smoothie.widgetFactory.preference

import com.smoothie.widgetFactory.CollapsingToolbarActivity
import com.smoothie.widgetFactory.R

open class PreferenceActivity(
    val preferencesResourceId: Int
) : CollapsingToolbarActivity(
    R.string.header_settings,
    PreferenceFragment()
)
