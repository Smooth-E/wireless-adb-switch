package com.smoothie.widgetFactory

open class PreferenceActivity(
    val preferencesResourceId: Int
) : CollapsingToolbarActivity(
    R.string.header_settings,
    PreferenceFragment()
)
