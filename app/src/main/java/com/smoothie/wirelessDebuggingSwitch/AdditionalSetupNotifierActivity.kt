package com.smoothie.wirelessDebuggingSwitch

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdditionalSetupNotifierActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 1244
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.layout_setup_notifier_bottom_sheet)
        dialog.setCancelable(false)

        val grantPermissionsListener = OnClickListener {
            val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            val context = this@AdditionalSetupNotifierActivity
            context.requestPermissions(permissions, REQUEST_CODE)
        }

        val grantPermissionsButton = dialog.findViewById<Button>(R.id.button_grant_permissions)
        grantPermissionsButton?.setOnClickListener(grantPermissionsListener)

        val learnMoreListener = OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/Smooth-E/wireless-adb-switch")
            startActivity(intent)
        }

        val learnMoreButton = dialog.findViewById<Button>(R.id.button_get_more_info)
        learnMoreButton?.setOnClickListener(learnMoreListener)

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("AdditionalSetupNotifierActivity", "Received a request code $requestCode and status $resultCode")
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                AdditionalSetupNotifier.showNotification()
                return
            }

            val goToSettings = DialogInterface.OnClickListener { _, _ ->

            }

            val dismiss = DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            }

            val style = com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
            MaterialAlertDialogBuilder(this, style)
                .setIcon(R.drawable.round_warning_24)
                .setTitle(R.string.label_crucial_permission)
                .setMessage(R.string.message_force_allow_notifications)
                .setPositiveButton(getString(R.string.label_got_to_settings), goToSettings)
                .setNeutralButton(getString(R.string.label_ignore), dismiss)
                .show()
        }
    }

}
