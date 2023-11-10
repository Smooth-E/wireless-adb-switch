package com.smoothie.widgetFactory.preference.slider

import com.google.android.material.slider.LabelFormatter

class SuffixLabelFormatter(private val prefix: String) : LabelFormatter {

    override fun getFormattedValue(value: Float): String =
        "$value$prefix"

}
