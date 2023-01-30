package com.smoothie.widgetFactory.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.slider.Slider
import com.smoothie.widgetFactory.R
import kotlin.math.abs

open class SliderPreference : Preference {

    companion object {
        private const val TAG = "SliderPreference"
    }

    private var isTrackingTouch = false
    private var sliderValueTextView: TextView? = null

    private lateinit var slider: Slider

    private var currentSliderValue = 0f

    var showSliderValue = true
    set(value) {
        if (field == value)
            return

        field = value
        notifyChanged()
    }

    var updatesContinuously = true
    var isAdjustableWithKeys = true

    var sliderStep = 1f
    set(value) {
        if (value != sliderStep) {
            field = (maximumValue - minimumValue).coerceAtMost(abs(value))
            notifyChanged()
        }
    }

    var minimumValue = 0f
    set(value) {
        val clampedValue = if (value > maximumValue) maximumValue else value

        if (clampedValue != minimumValue) {
            field = clampedValue
            notifyChanged()
        }
    }

    var maximumValue = 100f
    set(value) {
        val clampedValue = if (value < minimumValue) minimumValue else value

        if (clampedValue != field) {
            field = clampedValue
            notifyChanged()
        }
    }

    private val sliderChangeListener = Slider.OnChangeListener { slider, value, fromUser ->
        if (fromUser && (updatesContinuously || !isTrackingTouch))
            syncValueInternal(slider)
        else
            updateLabelValue(value + minimumValue)
    }

    private val sliderTouchListener = object : Slider.OnSliderTouchListener {

        override fun onStartTrackingTouch(slider: Slider) {
            isTrackingTouch = true
        }

        override fun onStopTrackingTouch(slider: Slider) {
            isTrackingTouch = false

            if (slider.value + minimumValue != getSliderValue())
                syncValueInternal(slider)
        }

    }

    private val sliderOnKeyListener = View.OnKeyListener { _, keyCode, event ->
        if (event.action != KeyEvent.ACTION_DOWN)
            return@OnKeyListener false

        val sideKeycodePressed =
            keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT

        if (!isAdjustableWithKeys && sideKeycodePressed)
            return@OnKeyListener false

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
            return@OnKeyListener false

        slider.onKeyDown(keyCode, event)
    }

    constructor(
        context: Context,
        attributes: AttributeSet?,
        defaultStyleAttribute: Int,
        defaultStyleResource: Int
    ) : super(context, attributes, defaultStyleAttribute, defaultStyleResource) {
        layoutResource = R.layout.preference_slider

        val obtainedAttributes = context.obtainStyledAttributes(
            attributes,
            R.styleable.SliderPreference,
            defaultStyleAttribute,
            defaultStyleResource
        )

        var attributeIndex = R.styleable.SliderPreference_android_valueFrom
        minimumValue = obtainedAttributes.getFloat(attributeIndex, 0f)

        attributeIndex = R.styleable.SliderPreference_android_valueTo
        maximumValue = obtainedAttributes.getFloat(attributeIndex, 100f)

        attributeIndex = R.styleable.SliderPreference_android_stepSize
        sliderStep = obtainedAttributes.getFloat(attributeIndex, 1f)

        attributeIndex = R.styleable.SliderPreference_adjustable
        isAdjustableWithKeys = obtainedAttributes.getBoolean(attributeIndex, true)

        attributeIndex = R.styleable.SliderPreference_showSliderValue
        showSliderValue = obtainedAttributes.getBoolean(attributeIndex, true)

        attributeIndex = R.styleable.SliderPreference_updatesContinuously
        updatesContinuously = obtainedAttributes.getBoolean(attributeIndex, true)

        obtainedAttributes.recycle()
    }

    constructor(context: Context, attributes: AttributeSet?, defaultStyleAttribute: Int)
            : this(context, attributes, defaultStyleAttribute, 0)

    constructor(context: Context, attributes: AttributeSet?)
            : this(context, attributes, R.attr.sliderPreferenceStyle)

    constructor(context: Context) : this(context, null)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.setOnKeyListener(sliderOnKeyListener)

        slider = holder.findViewById(R.id.slider) as Slider
        sliderValueTextView = holder.findViewById(R.id.slider_value) as TextView

        if (showSliderValue)
            sliderValueTextView?.visibility = View.VISIBLE
        else {
            sliderValueTextView?.visibility = View.GONE
            sliderValueTextView = null
        }

        slider.addOnChangeListener(sliderChangeListener)
        slider.addOnSliderTouchListener(sliderTouchListener)

        slider.valueTo = maximumValue - minimumValue

        if (sliderStep != 0f)
            slider.stepSize = sliderStep
        else
            sliderStep = slider.stepSize

        slider.value = getSliderValue() - minimumValue
        updateLabelValue(getSliderValue())
        slider.isEnabled = isEnabled
    }

    override fun onSetInitialValue(givenDefaultValue: Any?) {
        var defaultValue = givenDefaultValue

        if (defaultValue == null)
            defaultValue = 0

        setSliderValue(getPersistedFloat((defaultValue as Int).toFloat()))
    }

    override fun onGetDefaultValue(typedArray: TypedArray, index: Int): Any? =
        typedArray.getInt(index, 0)

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        if (isPersistent)
            return superState

        val state = SavedState(superState)

        state.sliderValue = getSliderValue()
        state.minimumValue = minimumValue
        state.maximumValue = maximumValue

        return state
    }

    override fun onRestoreInstanceState(savedState: Parcelable?) {
        if (savedState == null || savedState.javaClass != SavedState::class.java) {
            super.onRestoreInstanceState(savedState)
            return
        }

        val state = savedState as SavedState
        super.onRestoreInstanceState(state.superState)

        setSliderValue(state.sliderValue)
        minimumValue = state.minimumValue
        maximumValue = state.maximumValue

        notifyChanged()
    }

    fun setSliderValue(value: Float) {
        setValueInternal(value, true)
    }

    fun getSliderValue() = currentSliderValue


    private fun setValueInternal(newSliderValue: Float, notifyChanged: Boolean) {
        var clampedSliderValue = newSliderValue

        if (newSliderValue < minimumValue)
            clampedSliderValue = minimumValue
        else if (newSliderValue > maximumValue)
            clampedSliderValue = maximumValue

        if (newSliderValue != currentSliderValue) {
            currentSliderValue = clampedSliderValue
            updateLabelValue(currentSliderValue)
            persistFloat(newSliderValue)

            if (notifyChanged)
                notifyChanged()
        }
    }

    private fun syncValueInternal(slider: Slider) {
        val cachedSliderValue = minimumValue + slider.value

        if (cachedSliderValue == currentSliderValue)
            return

        if (callChangeListener(cachedSliderValue))
            setValueInternal(cachedSliderValue, false)
        else {
            slider.value = currentSliderValue - minimumValue
            updateLabelValue(currentSliderValue)
        }
    }

    fun updateLabelValue(value: Float) {
        sliderValueTextView?.text = value.toString()
    }

    private class SavedState : BaseSavedState {

        var sliderValue = 0f
        var minimumValue = 0f
        var maximumValue = 0f

        constructor(source: Parcel) : super(source) {
            sliderValue = source.readFloat()
            minimumValue = source.readInt().toFloat()
            maximumValue = source.readInt().toFloat()
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)

            dest.writeFloat(sliderValue)
            dest.writeFloat(minimumValue)
            dest.writeFloat(maximumValue)
        }

    }

}
