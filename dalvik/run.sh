#!/bin/bash
adb shell mkdir /mnt/sdcard/tmp
adb push foo.jar /mnt/sdcard/tmp
adb shell dalvikvm -cp /mnt/sdcard/tmp/foo.jar Foo
