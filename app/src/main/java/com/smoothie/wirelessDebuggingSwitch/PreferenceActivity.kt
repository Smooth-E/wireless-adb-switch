package com.smoothie.wirelessDebuggingSwitch

import com.smoothie.widgetFactory.CollapsingToolbarActivity
import com.smoothie.widgetFactory.PreferenceFragment

class PreferenceActivity : CollapsingToolbarActivity(
    PreferenceFragment(R.xml.preferences_app),
    R.string.header_settings
)
