#!/bin/bash
#point cp to a compatible android.jar for your target please
javac -classpath /Users/song/adk/android-sdk-mac_x86/platforms/android-10/android.jar Foo.java
dx --dex --output=foo.jar Foo.class
