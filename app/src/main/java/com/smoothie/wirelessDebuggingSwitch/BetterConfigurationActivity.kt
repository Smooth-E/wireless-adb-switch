package com.smoothie.wirelessDebuggingSwitch

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import com.smoothie.widgetFactory.configuration.WidgetConfigurationActivity
import com.topjohnwu.superuser.Shell
import java.io.File

abstract class BetterConfigurationActivity(
    preferencesResourceId: Int,
    previewAspectRatio: Float
) : WidgetConfigurationActivity(preferencesResourceId, previewAspectRatio) {

    companion object {
        private const val TAG = "BetterConfigurationActivity"
    }

    override fun getWallpaperOnNewerSdk(): Drawable? {
        try {
            if (Shell.isAppGrantedRoot() != true) {
                val message = getString(R.string.message_need_root_for_wallpaper)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                throw Exception("No root access granted to get wallpaper!")
            }

            val userIdCommand = "am get-current-user"
            val userId = Shell.cmd(userIdCommand).exec().out.joinToString().toInt()

            val file = File(dataDir, "wallpaper")
            file.createNewFile()

            val location = "/data/system/users/$userId/wallpaper_orig"
            val command = "cp \"$location\" \"${file.path}\""
            val result = Shell.cmd(command).exec()

            if (!result.isSuccess)
                throw Exception("Failed to get wallpaper!\n${result.err.joinToString()}")

            val array = file.readBytes()
            val bitmap = BitmapFactory.decodeByteArray(array, 0, array.size)

            file.delete()

            return BitmapDrawable(resources, bitmap)
        }
        catch (exception: Exception) {
            Log.w(TAG, "Filed to get wallpaper with root!")
            exception.printStackTrace()
            return null
        }
    }

}
