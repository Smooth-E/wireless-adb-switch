package com.smoothie.wirelessDebuggingSwitch

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smoothie.widgetFactory.CollapsingToolbarActivity
import com.topjohnwu.superuser.Shell

class GrantPermissionsActivity : CollapsingToolbarActivity(
    R.string.title_additional_setup,
    GrantPermissionsFragment(),
    false
) {

    companion object {
        fun startIfNeeded(context: Context) {
            if (!(isNotificationPermissionGranted(context) && hasSufficientPrivileges()))
                context.startActivity(Intent(context, GrantPermissionsActivity::class.java))
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val message = getString(R.string.message_grant_permission_before_leaving)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    class GrantPermissionsFragment : Fragment(R.layout.fragment_permissions) {

        companion object {
            private const val TAG = "GrantPermissionsFragment"
        }

        private val requestCode = 12345

        private lateinit var notificationsButton: Button
        private lateinit var rootAccessButton: Button
        private lateinit var shizukuButton: Button
        private lateinit var refreshShizukuStatusButton: Button
        private lateinit var continueButton: MaterialButton

        /**
         * Requests a permission to send notifications,
         * opens system settings if it is impossible to show the rationale.
         * Ensure that this function is onl called on API 33.
         */
        private val requestNotificationsPermission = OnClickListener {
            val currentApi = Build.VERSION.SDK_INT
            if (currentApi < Build.VERSION_CODES.TIRAMISU) {
                val message =
                    "requestNotificationsPermission OnClickListener called on API $currentApi, " +
                    "required API is Tiramisu (33)"
                Log.e(TAG, message)
                return@OnClickListener
            }

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
            Log.d("requestRootAccess", "Requesting the state")
            // This line will prompt the user to provide root access if it is unavailable
            val state = Shell.isAppGrantedRoot()
            Log.d("requestRootAccess", "Received state: $state")
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
            refreshShizukuStatusButton.setOnClickListener { updatePrivilegeLevelCards() }
            rootAccessButton.setOnClickListener { restartAppForRootAccessRefresh() }

            continueButton.fixTextAlignment()
            continueButton.setOnClickListener {
                if (Shell.isAppGrantedRoot() == true)
                    showMagiskNotificationsReminder(requireContext())
                else
                    this@GrantPermissionsFragment.requireActivity().finish()
            }

            updateAllCards()
        }

        override fun onResume() {
            super.onResume()
            updateAllCards()
        }

        private fun showMagiskNotificationsReminder(context: Context) {
            MaterialAlertDialogBuilder(context, centeredAlertDialogStyle)
                .setIcon(R.drawable.magisk)
                .setTitle(R.string.title_root_access_notifications)
                .setMessage(R.string.message_disable_magisk_notification)
                .setPositiveButton(R.string.label_got_it) { _, _ -> requireActivity().finish() }
                .show()
        }

        private fun updateAllCards() {
            updateNotificationsCard()
            updatePrivilegeLevelCards()
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
                updateContinueButton()
                return
            }
            else if (ShizukuUtilities.hasShizukuPermission()) {
                rootAccessButton.isEnabled = true
                rootAccessButton.text = getString(R.string.label_grant_permission)
                shizukuButton.isEnabled = false
                shizukuButton.text = getString(R.string.label_granted)
            }
            else {
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

            rootAccessButton.isEnabled = true
            rootAccessButton.text = getString(R.string.label_restart)

            updateContinueButton()
        }

        private fun updateContinueButton() {
            continueButton.isEnabled =
                isNotificationPermissionGranted(context) && hasSufficientPrivileges()
        }

        /**
         * Sets a shared preference and kills the app process because root access management
         * solutions like Magisk do not update the state of Shell.isAppGrantedRoot()
         * without a full restart
         */
        @SuppressLint("WrongConstant")
        private fun restartAppForRootAccessRefresh() {
            val manager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val time = System.currentTimeMillis() + 100
            val intent = PendingIntent.getActivity(
                requireActivity().baseContext,
                0,
                requireActivity().intent,
                requireActivity().intent.flags
            )
            manager.set(AlarmManager.RTC, time, intent)

            Process.killProcess(Process.myPid())
        }

    }
}
