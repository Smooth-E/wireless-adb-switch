package com.smoothie.wirelessDebuggingSwitch.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.children
import com.google.android.material.elevation.SurfaceColors
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.Utilities

class CollapsingTopBar : FrameLayout {

    companion object {
        private const val PLACEHOLDER_HEADER = "Placeholder Header"
        private const val DEFAULT_PRIMARY_ICON = R.drawable.ic_round_arrow_back_24
        private const val DEFAULT_SECONDARY_ICON = R.drawable.ic_round_settings_24
    }

    private var maxScroll = 50

    private lateinit var text: String

    private lateinit var topBarTextView: TextView
    private lateinit var primaryButton: View
    private lateinit var primaryIcon: ImageView
    private lateinit var secondaryButton: View
    private lateinit var secondaryIcon: ImageView

    private lateinit var scrollView: ScrollView
    private lateinit var contentTextView: TextView

    var primaryOnClickListener: OnClickListener? = null
        set(value) {
            setButtonOnClickListener(primaryButton, value)
            field = value
        }

    var secondaryOnClickListener: OnClickListener? = null
        set (value) {
            setButtonOnClickListener(secondaryButton, value)
            field = value
        }

    constructor(context: Context) : super(context) {
        buildView(context, null)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        buildView(context, attributeSet)
    }

    private fun buildView(context: Context, attributeSet: AttributeSet?) {
        val view = inflate(context, R.layout.collapsing_top_bar, this)

        topBarTextView = view.findViewById(R.id.text)
        primaryButton = view.findViewById(R.id.button_primary)
        primaryIcon = view.findViewById(R.id.icon_primary)
        secondaryButton = view.findViewById(R.id.button_secondary)
        secondaryIcon = view.findViewById(R.id.icon_secondary)

        val attributes =
            if (attributeSet != null)
                context.theme.obtainStyledAttributes(
                    attributeSet,
                    R.styleable.CollapsingTopBar,
                    0,
                    0
                )
            else
                null

        text = attributes?.getString(R.styleable.CollapsingTopBar_text) ?: PLACEHOLDER_HEADER
        topBarTextView.text = text

        primaryIcon.setImageResource(
            attributes?.getResourceId(
                R.styleable.CollapsingTopBar_icon_primary,
                DEFAULT_PRIMARY_ICON
            ) ?: DEFAULT_PRIMARY_ICON
        )

        secondaryIcon.setImageResource(
            attributes?.getResourceId(
                R.styleable.CollapsingTopBar_icon_secondary,
                DEFAULT_SECONDARY_ICON
            ) ?: DEFAULT_SECONDARY_ICON
        )
    }

    private fun setButtonOnClickListener(view: View, listener: OnClickListener?) {
        view.visibility = if (listener == null) View.GONE else View.VISIBLE
        view.setOnClickListener(listener)
        topBarTextView.setPadding(
            if (primaryButton.visibility == View.VISIBLE) 0 else Utilities.dp2px(context, 10f),
            topBarTextView.paddingTop,
            if (secondaryButton.visibility == View.VISIBLE) 0 else Utilities.dp2px(context, 10f),
            topBarTextView.paddingBottom
        )
    }

    private fun getSemiTransparentColor(alpha: Float, color: Color) : Int =
        Color.argb(alpha, color.red(), color.green(), color.blue())

    fun attachToScrollView(view: ScrollView) {
        scrollView = view
        contentTextView =
            View.inflate(context, R.layout.collapsing_top_bar_text, null) as TextView
        (scrollView.getChildAt(0) as ViewGroup).addView(contentTextView)
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            var ratio = scrollY.toFloat() / maxScroll
            if (ratio > 1)
                ratio = 1f
            val color = Color.valueOf(SurfaceColors.getColorForElevation(context, 8f))
            topBarTextView.setTextColor(getSemiTransparentColor(ratio, color))
            contentTextView.setTextColor(getSemiTransparentColor(1 - ratio, color))
        }
    }

}
