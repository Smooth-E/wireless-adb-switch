# -*- coding: utf-8 -*-

# This script checks your clipboard contents and connects to the wireless debugging bridge if requested
#
# Requirements:
# pyperclip
# win10toast

import pyperclip
import os
from win10toast import ToastNotifier


def main():
    if not pyperclip.is_available:
        print('Pyperclip is not available!')
        return

    connect_action_prefix = 'connect-wireless-debugging://'
    previous_clipboard = pyperclip.paste()
    
    toast = ToastNotifier()
    
    while True:
        new_clipboard = pyperclip.waitForNewPaste()
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
