/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This file is a modified version of Android Open Source Project's AndroidX SeekbarPreference

package com.smoothie.widgetFactory;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.slider.Slider;
import com.google.android.material.slider.Slider.OnChangeListener;
import com.google.android.material.slider.Slider.OnSliderTouchListener;

/**
 * Preference based on android.preference.SeekBarPreference but uses support preference as a base
 * . It contains a title and a {@link SeekBar} and an optional SeekBar value {@link TextView}.
 * The actual preference layout is customizable by setting {@code android:layout} on the
 * preference widget layout or {@code seekBarPreferenceStyle} attribute.
 *
 * <p>The {@link SeekBar} within the preference can be defined adjustable or not by setting {@code
 * adjustable} attribute. If adjustable, the preference will be responsive to DPAD left/right keys.
 * Otherwise, it skips those keys.
 *
 * <p>The {@link SeekBar} value view can be shown or disabled by setting {@code showSeekBarValue}
 * attribute to true or false, respectively.
 *
 * <p>Other {@link SeekBar} specific attributes (e.g. {@code title, summary, defaultValue, min,
 * max})
 * can be set directly on the preference widget layout.
 */
public class SliderPreference extends Preference {

    private static final String TAG = "SeekBarPreference";

    @SuppressWarnings("WeakerAccess") boolean isAdjustableWithKeys;
    @SuppressWarnings("WeakerAccess") boolean updatesContinuously;
    @SuppressWarnings("WeakerAccess") private float sliderValue;
    @SuppressWarnings("WeakerAccess") private float minimumValue;
    @SuppressWarnings("WeakerAccess") boolean isTrackingTouch;
    @SuppressWarnings("WeakerAccess") private Slider slider;
    private TextView sliderValueTextView;
    private boolean showSliderValue;
    private float sliderIncrement;
    private float maximumValue;

    private final OnChangeListener mSeekBarChangeListener = new OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            if (fromUser && (updatesContinuously || !isTrackingTouch))
                syncValueInternal(slider);
            else
                updateLabelValue(value + minimumValue);
        }
    };

    private final OnSliderTouchListener sliderTouchListener = new OnSliderTouchListener() {

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
            isTrackingTouch = true;
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            isTrackingTouch = false;

            if (slider.getValue() + minimumValue != sliderValue)
                syncValueInternal(slider);
        }

    };

    /**
     * Listener reacting to the user pressing DPAD left/right keys if {@code
     * adjustable} attribute is set to true; it transfers the key presses to the {@link SeekBar}
     * to be handled accordingly.
     */
    private final View.OnKeyListener mSeekBarKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (!isAdjustableWithKeys && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                // Right or left keys are pressed when in non-adjustable mode; Skip the keys.
                return false;
            }

            // We don't want to propagate the click keys down to the SeekBar view since it will
            // create the ripple effect for the thumb.
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                return false;
            }

            if (slider == null) {
                Log.e(TAG, "SeekBar view is null and hence cannot be adjusted.");
                return false;
            }
            return slider.onKeyDown(keyCode, event);
        }
    };

    public SliderPreference(
        @NonNull Context context,
        @Nullable AttributeSet attrs,
        int defStyleAttr,
        int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.setLayoutResource(R.layout.preference_slider);

        TypedArray typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.SliderPreference,
            defStyleAttr,
            defStyleRes
        );

        // The ordering of these two statements are important. If we want to set max first, we need
        // to perform the same steps by changing min/max to max/min as following:
        // mMax = typedArray.getInt(...) and setMin(...).
        minimumValue = typedArray.getInt(R.styleable.SliderPreference_android_valueFrom, 0);
        setMax(typedArray.getFloat(R.styleable.SliderPreference_android_valueTo, 100));

        setSliderIncrement(
            typedArray.getFloat(R.styleable.SliderPreference_android_stepSize, 0)
        );

        isAdjustableWithKeys =
                typedArray.getBoolean(R.styleable.SliderPreference_adjustable, true);

        showSliderValue =
                typedArray.getBoolean(R.styleable.SliderPreference_showSliderValue, false);

        updatesContinuously = typedArray
                .getBoolean(R.styleable.SliderPreference_updatesContinuously, false);

        typedArray.recycle();
    }

    public SliderPreference(
        @NonNull Context context,
        @Nullable AttributeSet attrs,
        int defStyleAttr
    ) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SliderPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.style);
    }

    public SliderPreference(@NonNull Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnKeyListener(mSeekBarKeyListener);

        slider = (Slider) holder.findViewById(R.id.slider);
        sliderValueTextView = (TextView) holder.findViewById(R.id.slider_value);

        if (showSliderValue)
            sliderValueTextView.setVisibility(View.VISIBLE);
        else {
            sliderValueTextView.setVisibility(View.GONE);
            sliderValueTextView = null;
        }

        if (slider == null) {
            Log.e(TAG, "SeekBar view is null in onBindViewHolder.");
            return;
        }

        slider.addOnChangeListener(mSeekBarChangeListener);
        slider.addOnSliderTouchListener(sliderTouchListener);

        slider.setValueTo(maximumValue - minimumValue);
        // If the increment is not zero, use that. Otherwise, use the default mKeyProgressIncrement
        // in AbsSeekBar when it's zero. This default increment value is set by AbsSeekBar
        // after calling setMax. That's why it's important to call setKeyProgressIncrement after
        // calling setMax() since setMax() can change the increment value.
        if (sliderIncrement != 0)
            slider.setStepSize(sliderIncrement);
        else
            sliderIncrement = slider.getStepSize();

        slider.setValue(sliderValue - minimumValue);
        updateLabelValue(sliderValue);
        slider.setEnabled(isEnabled());
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = 0;
        }

        Log.d(TAG, "Default value: " + defaultValue);
        setValue(getPersistedFloat(((Integer)defaultValue).floatValue()));
    }

    @Override
    protected @Nullable Object onGetDefaultValue(@NonNull TypedArray typedArray, int index) {
        return typedArray.getInt(index, 0);
    }

    /**
     * Gets the lower bound set on the {@link SeekBar}.
     *
     * @return The lower bound set
     */
    public float getMin() {
        return minimumValue;
    }

    /**
     * Sets the lower bound on the {@link SeekBar}.
     *
     * @param min The lower bound to set
     */
    public void setMin(float min) {
        if (min > maximumValue) {
            min = maximumValue;
        }
        if (min != minimumValue) {
            minimumValue = min;
            notifyChanged();
        }
    }

    /**
     * Returns the amount of increment change via each arrow key click. This value is derived from
     * user's specified increment value if it's not zero. Otherwise, the default value is picked
     * from the default mKeyProgressIncrement value in {@link android.widget.AbsSeekBar}.
     *
     * @return The amount of increment on the {@link SeekBar} performed after each user's arrow
     * key press
     */
    public final float getSliderIncrement() {
        return sliderIncrement;
    }

    /**
     * Sets the increment amount on the {@link SeekBar} for each arrow key press.
     *
     * @param seekBarIncrement The amount to increment or decrement when the user presses an
     *                         arrow key.
     */
    public final void setSliderIncrement(float seekBarIncrement) {
        if (seekBarIncrement != sliderIncrement) {
            sliderIncrement = Math.min(maximumValue - minimumValue, Math.abs(seekBarIncrement));
            notifyChanged();
        }
    }

    /**
     * Gets the upper bound set on the {@link SeekBar}.
     *
     * @return The upper bound set
     */
    public float getMax() {
        return maximumValue;
    }

    /**
     * Sets the upper bound on the {@link SeekBar}.
     *
     * @param max The upper bound to set
     */
    public final void setMax(float max) {
        if (max < minimumValue) {
            max = minimumValue;
        }
        if (max != maximumValue) {
            maximumValue = max;
            notifyChanged();
        }
    }

    /**
     * Gets whether the {@link SeekBar} should respond to the left/right keys.
     *
     * @return Whether the {@link SeekBar} should respond to the left/right keys
     */
    public boolean isAdjustableWithKeys() {
        return isAdjustableWithKeys;
    }

    /**
     * Sets whether the {@link SeekBar} should respond to the left/right keys.
     *
     * @param adjustableWithKeys Whether the {@link SeekBar} should respond to the left/right keys
     */
    public void setAdjustableWithKeys(boolean adjustableWithKeys) {
        isAdjustableWithKeys = adjustableWithKeys;
    }

    /**
     * Gets whether the {@link SliderPreference} should continuously save the {@link SeekBar} value
     * while it is being dragged. Note that when the value is true,
     * {@link Preference.OnPreferenceChangeListener} will be called continuously as well.
     *
     * @return Whether the {@link SliderPreference} should continuously save the {@link SeekBar}
     * value while it is being dragged
     * @see #setUpdatesContinuously(boolean)
     */
    public boolean getUpdatesContinuously() {
        return updatesContinuously;
    }

    /**
     * Sets whether the {@link SliderPreference} should continuously save the {@link SeekBar} value
     * while it is being dragged.
     *
     * @param updatesContinuously Whether the {@link SliderPreference} should continuously save
     *                            the {@link SeekBar} value while it is being dragged
     * @see #getUpdatesContinuously()
     */
    public void setUpdatesContinuously(boolean updatesContinuously) {
        this.updatesContinuously = updatesContinuously;
    }

    /**
     * Gets whether the current {@link SeekBar} value is displayed to the user.
     *
     * @return Whether the current {@link SeekBar} value is displayed to the user
     * @see #setShowSliderValue(boolean)
     */
    public boolean getShowSliderValue() {
        return showSliderValue;
    }

    /**
     * Sets whether the current {@link SeekBar} value is displayed to the user.
     *
     * @param showSliderValue Whether the current {@link SeekBar} value is displayed to the user
     * @see #getShowSliderValue()
     */
    public void setShowSliderValue(boolean showSliderValue) {
        this.showSliderValue = showSliderValue;
        notifyChanged();
    }

    private void setValueInternal(float sliderValue, boolean notifyChanged) {
        if (sliderValue < minimumValue) {
            sliderValue = minimumValue;
        }
        if (sliderValue > maximumValue) {
            sliderValue = maximumValue;
        }

        if (sliderValue != this.sliderValue) {
            this.sliderValue = sliderValue;
            updateLabelValue(this.sliderValue);
            persistFloat(sliderValue);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    /**
     * Gets the current progress of the {@link SeekBar}.
     *
     * @return The current progress of the {@link SeekBar}
     */
    public float getValue() {
        return sliderValue;
    }

    /**
     * Sets the current progress of the {@link SeekBar}.
     *
     * @param sliderValue The current progress of the {@link SeekBar}
     */
    public void setValue(float sliderValue) {
        setValueInternal(sliderValue, true);
    }

    /**
     * Persist the {@link SeekBar}'s SeekBar value if callChangeListener returns true, otherwise
     * set the {@link SeekBar}'s value to the stored value.
     */
    @SuppressWarnings("WeakerAccess")
    private void syncValueInternal(@NonNull Slider slider) {
        float sliderValue = minimumValue + slider.getValue();

        if (sliderValue != this.sliderValue) {
            if (callChangeListener(sliderValue))
                setValueInternal(sliderValue, false);
            else {
                slider.setValue(this.sliderValue - minimumValue);
                updateLabelValue(this.sliderValue);
            }
        }
    }

    /**
     * Attempts to update the TextView label that displays the current value.
     *
     * @param value the value to display next to the {@link SeekBar}
     */
    @SuppressWarnings("WeakerAccess")
    void updateLabelValue(float value) {
        if (sliderValueTextView != null)
            sliderValueTextView.setText(String.valueOf(value));
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.sliderValue = sliderValue;
        myState.minimumValue = minimumValue;
        myState.maximumValue = maximumValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        sliderValue = myState.sliderValue;
        minimumValue = myState.minimumValue;
        maximumValue = myState.maximumValue;
        notifyChanged();
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state of this preference.
     *
     * <p>It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        float sliderValue;
        float minimumValue;
        float maximumValue;

        SavedState(Parcel source) {
            super(source);

            // Restore the click counter
            sliderValue = source.readFloat();
            minimumValue = source.readInt();
            maximumValue = source.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            // Save the click counter
            dest.writeFloat(sliderValue);
            dest.writeFloat(minimumValue);
            dest.writeFloat(maximumValue);
        }
    }
}
