package com.smoothie.wirelessDebuggingSwitch

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.smoothie.widgetFactory.CollapsingToolbarActivity
import com.topjohnwu.superuser.Shell

class GrantPermissionsActivity : CollapsingToolbarActivity(
    R.string.title_welcome,
    GrantPermissionsFragment(),
    false
) {

    companion object {
        private fun isNotificationPermissionGranted(context: Context?): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                val permissionState = context?.checkSelfPermission(permission)
                permissionState == PackageManager.PERMISSION_GRANTED
            }
            else {
                true
            }

        fun startIfNeeded(context: Context) {
            val hasRootAccess = Shell.isAppGrantedRoot() == true
            val hasPrivileges = hasRootAccess || ShizukuUtilities.hasShizukuPermission()

            if (!(isNotificationPermissionGranted(context) && hasPrivileges))
                context.startActivity(Intent(context, GrantPermissionsActivity::class.java))
        }

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val message = getString(R.string.message_grant_permisson_berfore_leaving)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    class GrantPermissionsFragment : Fragment(R.layout.fragment_permissions) {

        private val requestCode = 12345

        private lateinit var notificationsButton: Button
        private lateinit var rootAccessButton: Button
        private lateinit var shizukuButton: Button
        private lateinit var refreshShizukuStatusButton: Button
        private lateinit var continueButton: MaterialButton

        private val requestNotificationsPermission = OnClickListener {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val showRationale = shouldShowRequestPermissionRationale(permission)

            if (!showRationale) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
                startActivity(intent)
            }
            else {
                requestPermissions(arrayOf(permission), requestCode)
            }
        }

        private val requestRootAccess = OnClickListener {
            Shell.isAppGrantedRoot()
            updatePrivilegeLevelCards()
        }

        private val requestShizukuPermission = OnClickListener {
            ShizukuUtilities.requestShizukuPermission {
                updatePrivilegeLevelCards()
            }
        }

        private val proceedToShizukuWebsite = OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://shizuku.rikka.app/guide/setup/")
            startActivity(intent)
        }

        private val refreshShizukuAvailability = OnClickListener {
            updatePrivilegeLevelCards()
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            notificationsButton = view.findViewById(R.id.grant_notifications)
            rootAccessButton = view.findViewById(R.id.grant_root_access)
            shizukuButton = view.findViewById(R.id.grant_shizuku)
            refreshShizukuStatusButton = view.findViewById(R.id.refresh_shizuku_status)
            continueButton = view.findViewById(R.id.button_continue)

            notificationsButton.setOnClickListener(requestNotificationsPermission)
            rootAccessButton.setOnClickListener(requestRootAccess)
            shizukuButton.setOnClickListener(requestShizukuPermission)
            refreshShizukuStatusButton.setOnClickListener(refreshShizukuAvailability)

            continueButton.fixTextAlignment()
            continueButton.setOnClickListener {
                this@GrantPermissionsFragment.requireActivity().finish()
            }

            updateNotificationsCard()
            updatePrivilegeLevelCards()
            updateContinueButton()
        }

        override fun onResume() {
            super.onResume()
            updateNotificationsCard()
        }

        private fun updateNotificationsCard() {
            val notificationsAllowed = isNotificationPermissionGranted(context)
            notificationsButton.isEnabled = !notificationsAllowed
            notificationsButton.text = getString(
                if (notificationsAllowed)
                    R.string.label_granted
                else
                    R.string.label_grant_permission
            )

            updateContinueButton()
        }

        private fun updatePrivilegeLevelCards() {
            if (Shell.isAppGrantedRoot() == true) {
                rootAccessButton.isEnabled = false
                rootAccessButton.text = getString(R.string.label_granted)
                shizukuButton.isEnabled = false
                shizukuButton.text = getString(R.string.label_not_needed)
            }
            else if (ShizukuUtilities.hasShizukuPermission()) {
                rootAccessButton.isEnabled = true
                rootAccessButton.text = getString(R.string.label_grant_permission)
                shizukuButton.isEnabled = false
                shizukuButton.text = getString(R.string.label_granted)
            }
            else {
                rootAccessButton.isEnabled = true
                rootAccessButton.text = getString(R.string.label_grant_permission)

                shizukuButton.isEnabled = true
                if (ShizukuUtilities.isShizukuAvailable()) {
                    shizukuButton.text = getString(R.string.label_grant_permission)
                    shizukuButton.setOnClickListener(requestShizukuPermission)
                }
                else {
                    shizukuButton.text = getString(R.string.label_setup_shizuku)
                    shizukuButton.setOnClickListener(proceedToShizukuWebsite)
                }
            }

            updateContinueButton()
        }

        private fun updateContinueButton() {
            continueButton.isEnabled =
                ShizukuUtilities.hasShizukuPermission() && isNotificationPermissionGranted(context)
        }

    }
}
