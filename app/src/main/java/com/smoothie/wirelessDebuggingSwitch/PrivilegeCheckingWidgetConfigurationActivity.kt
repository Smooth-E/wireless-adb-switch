package com.smoothie.wirelessDebuggingSwitch

import android.os.Bundle
import com.smoothie.widgetFactory.configuration.WidgetConfigurationActivity

abstract class PrivilegeCheckingWidgetConfigurationActivity(
    preferenceResourceId: Int,
    previewAspectRatio: Float
) : WidgetConfigurationActivity(preferenceResourceId, previewAspectRatio) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GrantPermissionsActivity.startIfNeeded(this)
    }

}
