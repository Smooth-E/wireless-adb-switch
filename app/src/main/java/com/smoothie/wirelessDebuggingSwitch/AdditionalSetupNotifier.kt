package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.topjohnwu.superuser.Shell
import rikka.shizuku.Shizuku

object AdditionalSetupNotifier {

    fun getPrivilegeLevel(context: Context): PrivilegeLevel {
        if (Shell.isAppGrantedRoot() == true)
            return PrivilegeLevel.Root

        if (ShizukuUtilities.isShizukuAvailable()) {
            val permission = Shizuku.checkSelfPermission()

            if (permission == PackageManager.PERMISSION_GRANTED)
                return PrivilegeLevel.Shizuku

            val key = context.getString(R.string.key_asked_shizuku_permission)
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val alreadyAsked = preferences.getBoolean(key, false)

            if (!alreadyAsked)
                requestShizukuPermission(context)

            return PrivilegeLevel.User
        }

        return PrivilegeLevel.User
    }

    private fun requestShizukuPermission(context: Context) {
        val message = context.getString(R.string.message_provide_shizuku_permission)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        val key = context.getString(R.string.key_asked_shizuku_permission)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putBoolean(key, true).apply()

        ShizukuUtilities.requestShizukuPermission { granted ->
            Log.d("AdditionalSetupNotifier", "Asked for a permission with a result $granted")
            if (!granted)
                askPermissionAndSendNotification(context)
        }
    }

    private fun askPermissionAndSendNotification(context: Context) {
        if (Build.VERSION.SDK_INT <= 32) {
            showNotification()
            return
        }

        val permission = android.Manifest.permission.POST_NOTIFICATIONS
        val allowed = ContextCompat.checkSelfPermission(context, permission)

        Log.d("AdditionalSetupNotifier", "Permission to send notifications is granted: $allowed")
        if (allowed == PackageManager.PERMISSION_GRANTED) {
            showNotification()
            return
        }

        val intent = Intent(context, AdditionalSetupNotifierActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun showNotification() {

    }

}
