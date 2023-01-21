package com.smoothie.wirelessDebuggingSwitch

import android.content.Context

class Utilities {

    companion object {

        fun dp2px(context: Context, dp: Float): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }

    }

}