package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell

object KdeConnect {

    private const val PACKAGE_NAME = "org.kde.kdeconnect_tp"
    private const val CLIPBOARD_ACTIVITY_NAME =
        "org.kde.kdeconnect.Plugins.ClibpoardPlugin.ClipboardFloatingActivity"

    fun isInstalled(context: Context): Boolean {
        val kdeConnectInstalled = isPackageInstalled(context, PACKAGE_NAME)
        Log.d("KdeConnect", "KDE Connect installation status is $kdeConnectInstalled")
        return kdeConnectInstalled
    }

    fun isClipboardSharingAvailable(context: Context): Boolean =
        isInstalled(context) && Shell.isAppGrantedRoot() == true

    /**
     * Synchronizes the clipboard data using KDE Connect.
     * Requires root access. Starts
     * [the following activity](https://invent.kde.org/network/kdeconnect-android/-/blob/aca039433c455b44b621dda077b940f26a732f25/src/org/kde/kdeconnect/Plugins/ClibpoardPlugin/ClipboardFloatingActivity.java)
     * of KDE Connect.
     */
    fun sendClipboard(): Shell.Result {
        val command =
            "am start " +
            "-n $PACKAGE_NAME/$CLIPBOARD_ACTIVITY_NAME " +
            "--ez SHOW_TOAST 0"

        return Shell.cmd(command).exec()
    }

}
