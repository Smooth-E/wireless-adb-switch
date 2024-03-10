package com.smoothie.wirelessDebuggingSwitch;

import android.content.Context;
import android.debug.IAdbManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Keep;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import rikka.shizuku.SystemServiceHelper;

public class UserService extends IUserService.Stub {

    private static final String TAG = "UserService";

    @Keep
    public UserService() {
        Log.d(TAG, "UserService initialized without a context");
    }

    @Keep
    public UserService(Context context) {
        Log.d(TAG, "UserService initialized with context");
    }

    @Override
    public void destroy() {
        Log.d(TAG, "UserService destroyed");
        System.exit(0);
    }

    @Override
    public String executeShellCommand(String command) {
        Process process = null;
        String output = "";

        try {
            Log.d(TAG, "Executing shell command: " + command);
            process = Runtime.getRuntime().exec(command);
            InputStream stream = process.getInputStream();
            process.waitFor();
            output = new BufferedReader(new InputStreamReader(stream)).readLine();

            // If a command produces no output, BufferedReader.readLine() will return null
            if (output == null)
                output = "";
        }
        catch (Exception exception) {
            Log.w(TAG, "Exception in executeShellCommand(...)");
            Log.w(TAG, Log.getStackTraceString(exception));
            output = "";
        }
        finally {
            if (process != null)
                process.destroy();
        }

        return output;
    }

    @Override
    public int getWirelessAdbPort() {
        Log.d(TAG, "Acquiring Wireless ADB port");
        IBinder service = SystemServiceHelper.getSystemService("adb");
        IAdbManager manager = IAdbManager.Stub.asInterface(service);
        int port = manager.getAdbWirelessPort();
        Log.d(TAG, "Wireless ADB port value: " + port);
        return port;
    }

}
