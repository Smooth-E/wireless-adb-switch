package com.smoothie.widgetFactory

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.doOnAttach

/**
 * This view will set its height to the value of a bottom screen inset as soon
 * as it is attached to a window. It is intended to be placed as the last view in a vertical
 * scrolling pane, so elements above it are always positioned above the navigation bar.
 *
 * The width of this view is defined manually
 *
 * @author Smooth E
 */
class BottomInsetHeightDistributor : FrameLayout {

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
        doOnAttach {
            val params = layoutParams
            params.height = getInsets(this).bottom
            layoutParams = params
        }
    }

}
