package com.smoothie.widgetFactory.view

import android.content.Context
import android.graphics.Insets
import android.util.AttributeSet
import android.view.View
import androidx.core.view.updateLayoutParams

class LeftInsetWidthDistributor : InsetSizeDistributor {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    override fun applyInsets(view: View, insets: Insets) {
        view.updateLayoutParams {
            this.width = insets.left
        }
    }

}
