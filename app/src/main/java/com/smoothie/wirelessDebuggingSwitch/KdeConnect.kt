package com.smoothie.wirelessDebuggingSwitch

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.topjohnwu.superuser.Shell

object KdeConnect {

    private const val PACKAGE_NAME = "org.kde.kdeconnect_tp"
    private const val CLIPBOARD_ACTIVITY_NAME =
        "org.kde.kdeconnect.Plugins.ClibpoardPlugin.ClipboardFloatingActivity"

    const val CLIPBOARD_PREFIX = "connect-wireless-debugging://"

    fun isInstalled(context: Context): Boolean {
        val kdeConnectInstalled = isPackageInstalled(context, PACKAGE_NAME)
        Log.d("KdeConnect", "KDE Connect installation status is $kdeConnectInstalled")
        return kdeConnectInstalled
    }

    // https://invent.kde.org/network/kdeconnect-android/-/blob/aca039433c455b44b621dda077b940f26a732f25/src/org/kde/kdeconnect/Plugins/ClibpoardPlugin/ClipboardFloatingActivity.java
    fun sendClipboard(context: Context, content: String): Shell.Result? {
        val label = "Clipboard for KDE Connect"
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, content))

        val command =
            "am start " +
            "-n $PACKAGE_NAME/$CLIPBOARD_ACTIVITY_NAME " +
            "--ez SHOW_TOAST 0"

        return executeShellCommand(command, PrivilegeLevel.Root)
    }

}
