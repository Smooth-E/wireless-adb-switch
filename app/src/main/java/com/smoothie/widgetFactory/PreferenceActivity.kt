package com.smoothie.widgetFactory

import com.smoothie.wirelessDebuggingSwitch.R

open class PreferenceActivity(
    val preferencesResourceId: Int
) : CollapsingToolbarActivity(
    R.string.header_settings,
    PreferenceFragment()
)
