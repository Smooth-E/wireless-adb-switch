package com.smoothie.wireless_adb_switch

import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream

open class SuperShell {

    private val process: Process
    private val outputStream: DataOutputStream
    private val inputStream: DataInputStream

    init {
        try {
            process = Runtime.getRuntime().exec("su")
        }
        catch (exception: Exception) {
            exception.printStackTrace()
            throw Exception("Could not get super privileges!")
        }
        outputStream = DataOutputStream(process.outputStream)
        inputStream = DataInputStream(process.inputStream)
    }

    protected fun execute(command: String): String {
        outputStream.writeBytes(command + "\n")
        outputStream.flush()
        val result = inputStream.readLine()
        Log.d("SuperShell", "Got command: $command\nResult: $result")
        return result
    }

}
