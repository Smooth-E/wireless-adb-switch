package com.smoothie.wirelessDebuggingSwitch

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell

object Utilities {

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
        context: Context,
        command: String,
        requiredPrivilegeLevel: PrivilegeLevel = PrivilegeLevel.Shizuku
    ): Shell.Result? {
        val privilegeLevel = AdditionalSetupNotifier.getPrivilegeLevel(context)

        val executableWithShizuku =
            requiredPrivilegeLevel == PrivilegeLevel.Shizuku ||
            requiredPrivilegeLevel == PrivilegeLevel.User

        // Did not consider the case when the required privilege level is User,
        // because WADBS is almost useless without either root or Shizuku.

        if (privilegeLevel == PrivilegeLevel.Root) {
            return Shell.cmd(command).exec()
        }
        else if (privilegeLevel == PrivilegeLevel.Shizuku && executableWithShizuku) {
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

}
