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
 * Get the highest privilege level available
 * and notify the user otherwise if it is lower than the required one.
 *
 * @param requiredPrivilegeLevel the required privileged level.
 * @param context if a [Context] is provided, the user will be notified about missing privileges
 *
 * @return highest privilege level available
 */
fun getPrivilegeLevel(
    requiredPrivilegeLevel: PrivilegeLevel = PrivilegeLevel.Shizuku,
    context: Context? = null,
) : PrivilegeLevel {
    val privilegeLevel: PrivilegeLevel =
        if (Shell.isAppGrantedRoot() == true)
            PrivilegeLevel.Root
        else if (ShizukuUtilities.hasShizukuPermission())
            PrivilegeLevel.Shizuku
        else
            PrivilegeLevel.User

    if (privilegeLevel.ordinal < requiredPrivilegeLevel.ordinal) {
        Log.e("Utilities.getPrivilegeLevel", "Required privilege level too high!")
        if (context != null)
            sendMissingPrivilegesNotification(context)
    }

    return privilegeLevel
}

/**
 * Check whether you have a required privilege level and notify the user otherwise.
 * @param requiredPrivilegeLevel the required privileged level.
 * @param context if a [Context] is provided, the user will be notified about missing privileges
 *
 * @return whether the available privilegeLevel is higher or equal to [requiredPrivilegeLevel]
 */
fun hasSufficientPrivileges(
    requiredPrivilegeLevel: PrivilegeLevel = PrivilegeLevel.Shizuku,
    context: Context? = null,
): Boolean =
    getPrivilegeLevel(
        requiredPrivilegeLevel,
        context,
    ).ordinal >= requiredPrivilegeLevel.ordinal

/**
 * Execute a shell command. This method will choose between Shizuku and root execution.
 * If neither are possible the result will be null.
 *
 * Inspired by a similar implementation in
 * [Better Internet Tiles](https://github.com/CasperVerswijvelt/Better-Internet-Tiles).
 *
 * @param command a command to execute
 * @param context if a context is provided, the user will be notified about missing privileges
 * @param requiredPrivilegeLevel a required level of privileges
 *
 * @return the output of a command if it executed successfully and an empty [String] otherwise
 */
fun executeShellCommand(
    command: String,
    context: Context? = null,
    requiredPrivilegeLevel: PrivilegeLevel = PrivilegeLevel.Shizuku,
): String {
    val privilegeLevel = getPrivilegeLevel(requiredPrivilegeLevel, context)

    if (privilegeLevel == PrivilegeLevel.Root)
        return Shell.cmd(command).exec().out.toString()
    else if (privilegeLevel == PrivilegeLevel.Shizuku)
        return ShizukuUtilities.executeCommand(command)

    val message =
        "Error executing a shell command! Neither Shizuku or root access are present."
    Log.d("Utilities.executeShellCommand", message)
    return ""
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
        .Builder(context, WADBS.PRIVILEGE_NOTIFICATION_CHANNEL_ID)
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
            .notify(WADBS.PRIVILEGE_NOTIFICATION_ID, notification)
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

fun isPackageInstalled(context: Context, name: String): Boolean {
    val packages = context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
    for (installedPackage in packages) {
        if (installedPackage.packageName == name)
            return true
    }
    return false
}

fun copyText(context: Context, label: String, content: String) {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, content))
}
