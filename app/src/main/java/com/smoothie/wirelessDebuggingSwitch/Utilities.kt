package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
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
 * @return [Shell.Result] if either Shizuku or root are present and null otherwise.
 */
fun executeShellCommand(
    command: String,
    requiredPrivilegeLevel: PrivilegeLevel = PrivilegeLevel.Shizuku
): Shell.Result? {
    val privilegeLevel = getPrivilegeLevel()

    if (privilegeLevel.ordinal <= requiredPrivilegeLevel.ordinal) {
        Log.e("Utilities.executeShellCommand", "Required privilege level too high!")
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