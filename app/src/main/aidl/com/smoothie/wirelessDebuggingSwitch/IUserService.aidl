package com.smoothie.wirelessDebuggingSwitch;

/**
 * Shizuku's UserService implementation.
 * @author Smooth-E
 */
interface IUserService {

    /** Default destroy() method, requireed by Shizuku */
    void destroy() = 16777114;

    /**
     * Executes a shell command from the identity of Shizuku's UserService
     * (most likely Android Shell, uid 2000).
     *
     * @param command - a command to be executed
     * @return - the output of command execution in form of a @code{ String }
     */
    String executeShellCommand(String command) = 0;

    /** Returns Wireless ADB port number or -1 if Wireless ADB is disabled */
    int getWirelessAdbPort() = 1;
}
