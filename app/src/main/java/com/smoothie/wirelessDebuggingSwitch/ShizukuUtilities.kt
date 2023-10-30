package com.smoothie.wirelessDebuggingSwitch

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
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
    private const val TAG = "ShizukuUtilities"
    private var userService: IUserService? = null

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if (binder == null || !binder.pingBinder()) {
                Log.d(TAG, "Received null or dead binder")
                return
            }

            Log.d(TAG, "UserService connected")
            userService = IUserService.Stub.asInterface(binder)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Intentional no-op
        }

    }

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

    fun executeCommand(command: String): String =
        if (ensureUserService()) {
            Log.d(TAG, "userService == null? ${userService == null}")
            val result = userService!!.executeShellCommand(command)
            Log.d(TAG, "result == null? ${result == null}")
            result
        }
        else ""

    fun getWirelessAdbPort(): String =
        if (ensureUserService()) userService!!.wirelessAdbPort.toString() else ""

    /**
     * Binds Shizuku's [UserService] if it does not exists.
     * @return `true` if the service is already present and `false` otherwise.
     */
    private fun ensureUserService(): Boolean {
        if (userService != null) {
            return true
        }

        Log.d(TAG, "UserService not ready. Binding.")

        val component = ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name)
        val userServiceArgs = Shizuku.UserServiceArgs(component)
            .daemon(false)
            .processNameSuffix("service")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
        Shizuku.bindUserService(userServiceArgs, serviceConnection)

        return false
    }

}
