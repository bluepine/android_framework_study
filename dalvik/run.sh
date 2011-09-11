#!/bin/bash
adb shell mkdir /sdcard/tmp/
adb push foo.jar /sdcard/tmp/
#adb shell dalvikvm -cp /sdcard/tmp/foo.jar Foo #this one won't work if you need android framework libraries. you need to ask zygote to initialze the env for you. one way is to use dvz utility.
adb shell dvz --help
adb shell dvz -classpath /sdcard/tmp/foo.jar Foo hi