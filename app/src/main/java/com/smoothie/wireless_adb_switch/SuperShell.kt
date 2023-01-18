package com.smoothie.wireless_adb_switch

import java.io.DataInputStream
import java.io.DataOutputStream

open class SuperShell {

    companion object {

        @JvmStatic
        protected fun execute(command: String): String {
            val process: Process
            try {
                process = Runtime.getRuntime().exec("su")
            }
            catch (exception: Exception) {
                exception.printStackTrace()
                throw Exception("Could not get super privileges!")
            }
            val outputStream = DataOutputStream(process.outputStream)
            val inputStream = DataInputStream(process.inputStream)

            outputStream.writeBytes(command + "\n")
            outputStream.flush()
            var result = ""
            while (result.isEmpty())
                result = inputStream.readLine()
            process.destroy()
            return result
        }

    }
}
