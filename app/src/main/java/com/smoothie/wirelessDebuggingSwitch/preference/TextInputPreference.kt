package com.smoothie.wirelessDebuggingSwitch.preference

import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.smoothie.wirelessDebuggingSwitch.R

class TextInputPreference : Preference {

    private var inputFieldEditText: TextInputEditText? = null

    private var text: String = ""
        set(value) {
            if (value == field)
                return

            field = value
            notifyChanged()
        }

    private val textWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Intentional no-op
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (callChangeListener(s.toString())) {
                persistString(s.toString())
                notifyChanged()
            }
        }

        override fun afterTextChanged(s: Editable?) {
            // Intentional no-op
        }

    }

    constructor(
        context: Context,
        attributes: AttributeSet?,
        defaultStyleAttribute: Int,
        defaultStyleResource: Int
    ) : super(context, attributes, defaultStyleAttribute, defaultStyleResource) {
        layoutResource = R.layout.preference_text_input
    }

    constructor(context: Context, attributes: AttributeSet?, defaultStyleAttribute: Int)
            : this(context, attributes, defaultStyleAttribute, 0)

    constructor(context: Context, attributes: AttributeSet?)
            : this(context, attributes, R.attr.textInputPreferenceStyle)

    constructor(context: Context) : this(context, null)


    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        inputFieldEditText = holder.findViewById(R.id.text_input_edit_text) as TextInputEditText

        if (inputFieldEditText == null || inputFieldEditText?.isFocused == true)
            return

        inputFieldEditText?.setText(text)
        inputFieldEditText?.addTextChangedListener(textWatcher)
        (holder.findViewById(R.id.text_input_layout) as TextInputLayout).hint = title
    }

    override fun onGetDefaultValue(typedArray: TypedArray, index: Int): Any? =
        typedArray.getString(index)

    override fun onSetInitialValue(defaultValue: Any?) {
        val value = defaultValue ?: ""
        text = getPersistedString(value as String)
    }

}
