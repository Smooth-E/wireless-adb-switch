# -*- coding: utf-8 -*-

# This script checks your clipboard contents and connects to the wireless debugging bridge if requested
#
# Requirements:
# pyperclip
# win10toast

from win10toast import ToastNotifier
import pyperclip
import os
import time


def waitForNewPaste(timeout=None):
    """
    This function is taken from pyperclip 1.8.2 as it was removed in later releases.
    
    This function call blocks until a new text string exists on the
    clipboard that is different from the text that was there when the function
    was first called. It returns this text.

    This function raises PyperclipTimeoutException if timeout was set to
    a number of seconds that has elapsed without non-empty text being put on
    the clipboard.
    """
    
    startTime = time.time()
    originalText = pyperclip.paste()
    while True:
        currentText = pyperclip.paste()
        if currentText != originalText:
            return currentText
        time.sleep(0.01)

        if timeout is not None and time.time() > startTime + timeout:
            raise pyperclip.PyperclipTimeoutException('waitForNewPaste() timed out after ' + str(timeout) + ' seconds.')


def main():
    if not pyperclip.is_available:
        print('Pyperclip is not available!')
        return

    connect_action_prefix = 'connect-wireless-debugging://'
    previous_clipboard = pyperclip.paste()
    
    toast = ToastNotifier()
    
    while True:
        new_clipboard = waitForNewPaste()
        print('New item copied: ' + new_clipboard)
        
        if not new_clipboard.startswith(connect_action_prefix):
            previous_clipboard = new_clipboard
            continue


        address = new_clipboard.removeprefix(connect_action_prefix)
        result = os.popen('adb connect ' + address).readline()
        result = result[0].upper() + result[1:]
        
        if result.startswith('Connected to '):
            icon = 'icons8-success.png'
            status = 'Success'
            pyperclip.copy(previous_clipboard)
        else:
            icon = 'icons8-warning.png'
            status = 'Warning'
            raw_connection_data = new_clipboard.removeprefix(connect_action_prefix)
            pyperclip.copy(raw_connection_data)
        
        toast.show_toast(status, result, icon, 3, False)



if __name__ == '__main__':
    main()
