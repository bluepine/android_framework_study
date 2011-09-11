#!/bin/bash
/Users/song/adk/android-ndk-r6b/ndk-build
adb shell mkdir -p /tmp/
adb push ../dalvik/foo.jar /tmp/
adb push libs/armeabi/dvz-jni /tmp/
adb shell chmod a+x /tmp/dvz-jni
#adb shell dalvikvm -cp /tmp/foo.jar Foo #this one won't work if you need android framework libraries. you need to ask zygote to initialze the env for you. one way is to use dvz utility.
#adb shell /tmp/dvz-jni --help
adb shell /tmp/dvz-jni -classpath /tmp/foo.jar Foo
