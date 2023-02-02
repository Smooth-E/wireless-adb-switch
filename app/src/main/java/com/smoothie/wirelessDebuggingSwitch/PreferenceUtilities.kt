package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtilities {

    fun decideBetweenTwo(
        preferences: SharedPreferences,
        booleanKey: String,
        object1: Any?,
        object2: Any?,
        defaultValue: Boolean = false
    ): Any? {
        return if (preferences.getBoolean(booleanKey, defaultValue))
            object1
        else
            object2
    }

    fun getLightOrDarkTextColor(context: Context, preferences: SharedPreferences): Int {
        val key = context.getString(R.string.key_use_light_text)
        val useLightText = preferences.getBoolean(key, false)

        val colorId =
            if (useLightText)
                R.color.colorSurface
            else
                R.color.colorOnSurface

        val theme = context.theme
        return theme.resources.getColor(colorId, theme)
    }

}
