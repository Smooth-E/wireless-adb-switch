package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.content.SharedPreferences

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
