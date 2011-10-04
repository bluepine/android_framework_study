/home/song/android-ndk-r6/ndk-build
adb push libs/armeabi/pm_dump /cache
adb shell chmod 777 /cache/pm_dump
adb shell /cache/pm_dump 24675 0x8000 0x800 /cache/memdump
adb pull /cache/memdump
