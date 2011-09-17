/home/song/android-ndk-r6/ndk-build
adb push libs/armeabi/ptracer /cache
adb shell chmod 777 /cache/ptracer
adb shell /cache/ptracer 7013
