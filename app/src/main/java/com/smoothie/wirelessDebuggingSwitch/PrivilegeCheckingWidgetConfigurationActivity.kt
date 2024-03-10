package com.smoothie.wirelessDebuggingSwitch

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smoothie.widgetFactory.configuration.WidgetConfigurationActivity

abstract class PrivilegeCheckingWidgetConfigurationActivity(
    preferenceResourceId: Int,
    previewAspectRatio: Float
) : WidgetConfigurationActivity(preferenceResourceId, previewAspectRatio) {

    private val proceedToSetupOnClickListener = DialogInterface.OnClickListener { _, _ ->
        val intent = Intent(this, SettingsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!GrantPermissionsActivity.shouldBeStarted(this))
            return

        val centeredDialogStyle =
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered

        MaterialAlertDialogBuilder(this, centeredDialogStyle)
            .setIcon(R.drawable.round_phonelink_setup_colored_24)
            .setTitle(R.string.title_additional_setup)
            .setMessage(R.string.message_before_adding_widgets)
            .setPositiveButton(R.string.label_proceed, proceedToSetupOnClickListener)
            .setCancelable(false)
            .show()
    }

}
