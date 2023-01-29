package com.smoothie.widgetFactory

import android.content.Context

object Utilities {

    fun getSharedPreferencesKey(context: Context, id: Int): String =
        context.getString(id)

    fun dp2px(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    fun getDimen(context: Context, resource: Int): Int =
        context.resources.getDimensionPixelSize(resource)

}
