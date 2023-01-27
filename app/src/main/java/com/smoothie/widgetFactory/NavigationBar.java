package com.smoothie.widgetFactory;

// Source: https://gist.github.com/Thorsten1976/07d61b3f697364e5f1c08ae076641d58

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class NavigationBar {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
        {
            MODE_THREE_BUTTONS,
            MODE_TWO_BUTTONS,
            MODE_GESTURES
        }
    )
    @interface NavigationBarInteractionMode {}

    /**
     * Classic three-button navigation (Back, Home, Recent Apps)
     */
    public static final int MODE_THREE_BUTTONS = 0;
    /**
     * Two-button navigation (Android P navigation mode: Back, combined Home and Recent Apps)
     */
    public static final int MODE_TWO_BUTTONS = 1;
    /**
     * Full screen gesture mode (introduced with Android Q)
     */
    public static final int MODE_GESTURES = 2;

    NavigationBar() {}

    /**
     * Returns the interaction mode of the system navigation bar as defined by
     * {@link NavigationBarInteractionMode}. Depending on the Android version and OEM implementation,
     * users might change the interaction mode via system settings: System>Gestures>System Navigation.
     * This can lead to conflicts with apps that use specific system-gestures internally for
     * navigation (i. e. swiping), especially if the Android 10 full screen gesture mode is enabled.
     * <p>
     * Before Android P the system has used a classic three button navigation. Starting with Android P
     * a two-button-based interaction mode was introduced (also referred as Android P navigation).
     * <p>
     * Android Q changed the interaction and navigation concept to a gesture approach, the so called
     * full screen gesture mode: system-wide gestures are used to navigate within an app or to
     * interact with the system (i. e. back-navigation, open the home-screen, changing apps, or toggle
     * a fullscreen mode).
     * <p>
     * Based on <a href="https://stackoverflow.com/questions/56689210/how-to-detect-full-screen-gesture-mode-in-android-10">
     *     https://stackoverflow.com/questions/56689210/how-to-detect-full-screen-gesture-mode-in-android-10</a>
     *
     * @param context The {@link Context} that is used to read the resource configuration.
     * @return the {@link NavigationBarInteractionMode}
     * @see #MODE_THREE_BUTTONS
     * @see #MODE_TWO_BUTTONS
     * @see #MODE_GESTURES
     */
    @NavigationBarInteractionMode
    public static int getInteractionMode(@NonNull final Context context) {
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
        return resourceId > 0 ? resources.getInteger(resourceId) : MODE_THREE_BUTTONS;
    }
}
