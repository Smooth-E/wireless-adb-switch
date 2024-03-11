package com.smoothie.widgetFactory.view

import android.content.Context
import android.graphics.Insets
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout

abstract class InsetSizeDistributor : FrameLayout {

    constructor(context: Context) : super(context) {
        applyLayoutParameters()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        applyLayoutParameters()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        applyLayoutParameters()
    }

    private fun applyLayoutParameters() {
        setOnApplyWindowInsetsListener { view, insets ->
            val mask = WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            val actualInsets = insets.getInsetsIgnoringVisibility(mask)
            applyInsets(view, actualInsets)
            insets
        }
    }

    protected abstract fun applyInsets(view: View, insets: Insets)

}
