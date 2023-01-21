package com.smoothie.wirelessDebuggingSwitch

class PreferenceActivity : CollapsingToolbarActivity(
    PreferenceFragment(R.xml.preferences_app),
    R.string.header_settings
)
