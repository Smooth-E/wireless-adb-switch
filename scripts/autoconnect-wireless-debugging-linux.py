#!/bin/python
# -*- coding: utf-8 -*-

# This script checks your clipboard contents and connects to the wireless debugging bridge if requested
#
# Requirements:
# pyperclip

from threading import Thread
import pyperclip
import os


def main():
    if not pyperclip.is_available:
        return

    connect_action_prefix = 'connect-wireless-debugging://'
    previous_clipboard = pyperclip.paste()

    while True:
        new_clipboard = pyperclip.waitForNewPaste()
        print('New item copied: ' + new_clipboard)
        
        if not new_clipboard.startswith(connect_action_prefix):
            previous_clipboard = new_clipboard
            continue

        pyperclip.copy(previous_clipboard)

        address = new_clipboard.removeprefix(connect_action_prefix)
        result = os.popen('adb connect ' + address).readline()
        result = result[0].upper() + result[1:]
        
        if result.startswith('Connected to '):
            icon = 'nm-device-wireless'
        else:
            icon = 'error'

        command = 'notify-send' + \
            ' --expire-time 3000' + \
            ' --icon ' + icon + \
            ' --app-name "Android Wireless Debugging"' + \
            ' "' + result + '"'
        
        # 'notify-send' blocks the thread, so it is invoked in the background
        Thread(target=os.system, args=[command]).start()


if __name__ == '__main__':
    main()
