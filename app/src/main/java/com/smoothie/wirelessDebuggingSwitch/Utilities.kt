package com.smoothie.wirelessDebuggingSwitch

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.button.MaterialButton
import com.topjohnwu.superuser.Shell

val centeredAlertDialogStyle =
    com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered

/**
 * Execute a shell command. This method will choose between Shizuku and root execution.
 * If neither are possible the result will be null.
 *
 * Originally written by [CasperVerswijvelt](https://github.com/CasperVerswijvelt)
 * for [Better Internet Tiles](https://github.com/CasperVerswijvelt/Better-Internet-Tiles).
 *
 * @param command a command to execute
 * @param requiredPrivilegeLevel a required level of privileges
 * @param context context used to send the notification
 * @param notifyUser whether to notify the user if required privilege level is too high
 * @return [Shell.Result] if either Shizuku or root are present and null otherwise.
 */
fun executeShellCommand(
    context: Context,
    command: String,
    requiredPrivilegeLevel: PrivilegeLevel = PrivilegeLevel.Shizuku,
    notifyUser: Boolean = true,
): Shell.Result? {
    val privilegeLevel = getPrivilegeLevel()

    if (privilegeLevel.ordinal < requiredPrivilegeLevel.ordinal) {
        Log.e("Utilities.executeShellCommand", "Required privilege level too high!")
        if (notifyUser)
            sendMissingPrivilegesNotification(context)
        return null
    }

    if (privilegeLevel == PrivilegeLevel.Root) {
        return Shell.cmd(command).exec()
    }
    else if (privilegeLevel == PrivilegeLevel.Shizuku) {
        val process = ShizukuUtilities.executeCommand(command)

        return object : Shell.Result() {

            override fun getOut(): MutableList<String> {
                return process
                    .inputStream.bufferedReader()
                    .use { it.readText() }
                    .split("\n".toRegex())
                    .toMutableList()
            }

            override fun getErr(): MutableList<String> {
                return process
                    .errorStream.bufferedReader()
                    .use { it.readText() }
                    .split("\n".toRegex())
                    .toMutableList()
            }

            override fun getCode(): Int {
                return process.exitValue()
            }
        }
    }

    val message =
        "Error executing a shell command! Neither Shizuku or root access are present."
    Log.d("Utilities.executeShellCommand", message)
    return null
}

@SuppressLint("MissingPermission")
private fun sendMissingPrivilegesNotification(context: Context) {
    Log.d("sendMissingPrivilegesNotification", "Sending the notification!")

    val notificationMessageLong = context.getString(R.string.notification_message_long)

    val intent = Intent(context, KillAppBroadcastReceiver::class.java)
    intent.action = KillAppBroadcastReceiver.INTENT_ACTION
    val flag = PendingIntent.FLAG_IMMUTABLE
    val restartAppIntent = PendingIntent.getBroadcast(context, 0, intent, flag)

    val restartAppAction = NotificationCompat.Action
        .Builder(null, context.getString(R.string.notification_button), restartAppIntent)
        .build()

    val notification = NotificationCompat
        .Builder(context, CustomApplication.PRIVILEGE_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.app_icon_foreground)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentText(context.getString(R.string.notification_message_short))
        .setStyle(NotificationCompat.BigTextStyle().bigText(notificationMessageLong))
        .addAction(restartAppAction)
        .setOngoing(true)
        .build()

    // We do not request the permission here because if it isn't granted yet, then user probably
    // didn't set up the app, yet, and if it is disabled by the user then there
    // is no point to request it
    if (isNotificationPermissionGranted(context)) {
        NotificationManagerCompat.from(context)
            .notify(CustomApplication.PRIVILEGE_NOTIFICATION_ID, notification)
    }
}

/**
 * Checks whether the permission to post notifications is granted.
 * Does not request the permission in case it is not.
 * @param context context to check the permission status against
 * @return whether the permission to post notifications is granted or not
 */
fun isNotificationPermissionGranted(context: Context?): Boolean {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU)
        return true

    val permission = Manifest.permission.POST_NOTIFICATIONS
    val permissionState = context?.checkSelfPermission(permission)
    return permissionState == PackageManager.PERMISSION_GRANTED
}

/**
 * Fixes text centering when a button has an icon aligned to the side.
 * [Source](https://github.com/material-components/material-components-android/issues/1996#issuecomment-762233981)
 */
fun MaterialButton.fixTextAlignment() {
    iconPadding = -(icon?.intrinsicWidth ?: 0)
}

fun getPrivilegeLevel(): PrivilegeLevel {
    if (Shell.isAppGrantedRoot() == true)
        return PrivilegeLevel.Root
    else if (ShizukuUtilities.hasShizukuPermission())
        return PrivilegeLevel.Shizuku
    return PrivilegeLevel.User
}

fun isPackageInstalled(context: Context, name: String): Boolean {
    val packages = context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
    for (installedPackage in packages) {
        if (installedPackage.packageName == name)
            return true
    }
    return false
}

fun hasSufficientPrivileges(): Boolean =
    Shell.isAppGrantedRoot() == true || ShizukuUtilities.hasShizukuPermission()

fun copyText(context: Context, label: String, content: String) {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, content))
}
