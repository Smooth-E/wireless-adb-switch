package com.smoothie.wirelessDebuggingSwitch

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/**
 * Utility class for working with Shizuku.
 *
 * Special thanks to [Casper Verswijvelt](https://github.com/CasperVerswijvelt) and their
 * [Better Internet Tiles app](https://github.com/CasperVerswijvelt/Better-Internet-Tiles/tree/main)
 * for being a reference implementation.
 *
 * @author Smooth-E
 */
object ShizukuUtilities {

    const val REQUEST_CODE = 1311

    fun isShizukuAvailable(): Boolean =
        Shizuku.pingBinder() && (Shizuku.getVersion() >= 11 && !Shizuku.isPreV11())

    fun hasShizukuPermission(): Boolean =
        isShizukuAvailable() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    fun requestShizukuPermission(callback: (granted: Boolean) -> Unit) {
        Shizuku.addRequestPermissionResultListener(object :
            Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                Shizuku.removeRequestPermissionResultListener(this)
                callback(grantResult == PackageManager.PERMISSION_GRANTED)
            }
        })

        Shizuku.requestPermission(REQUEST_CODE)
    }

    fun executeCommand(command: String): Process {
        val process = Shizuku.newProcess(
            command.split(' ').toTypedArray(),
            null,
            null
        )
        process.waitFor()
        return process
    }

}
