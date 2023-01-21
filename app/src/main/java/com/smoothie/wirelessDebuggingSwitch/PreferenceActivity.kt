package com.smoothie.wirelessDebuggingSwitch

import com.smoothie.wirelessDebuggingSwitch.core.CollapsingToolbarActivity
import com.smoothie.wirelessDebuggingSwitch.core.PreferenceFragment

class PreferenceActivity : CollapsingToolbarActivity(
    PreferenceFragment(R.xml.preferences_app),
    R.string.header_settings
)
