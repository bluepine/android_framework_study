#!/bin/bash
device=HT9A4LV01921
#device=033c20c142017617
set -x
/Users/song/adk/android-ndk-r6b/ndk-build
adb -s $device shell mkdir -p /mnt/sdcard/tmp
adb -s $device push ../dalvik/foo.jar /mnt/sdcard/tmp
adb -s $device push libs/armeabi/dvz-jni /data/local/tmp/
adb -s $device shell chmod a+x /data/local/tmp/dvz-jni
#adb -s $device shell dalvikvm -cp /data/local/tmp/foo.jar Foo #this one won't work if you need android framework libraries. you need to ask zygote to initialze the env for you. one way is to use dvz utility.
#adb -s $device shell /data/local/tmp/dvz-jni --help
adb -s $device shell /data/local/tmp/dvz-jni -classpath /mnt/sdcard/tmp/foo.jar Foo
