#!/bin/bash
adb push foo.jar /sdcard
adb shell dalvikvm -cp /sdcard/foo.jar Foo
