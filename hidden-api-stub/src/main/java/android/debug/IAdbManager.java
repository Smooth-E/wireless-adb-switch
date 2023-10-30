package android.debug;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IAdbManager extends IInterface {

    abstract class Stub extends Binder implements IAdbManager {

        public static IAdbManager asInterface(IBinder binder) {
            throw new UnsupportedOperationException("Stub!");
        }

    }

    /** Returns Wireless ADB port or -1 if Wireless ADB is disabled */
    int getAdbWirelessPort();

}
