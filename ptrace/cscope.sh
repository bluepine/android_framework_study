#!/bin/sh
#set -x
current=`pwd`
rm -rf cscope
mkdir cscope
find $current/* -name '*.c' > $current/cscope/cscope.files
find $current/* -name '*.cpp' > $current/cscope/cscope.files
find $current/* -name '*.h' >> $current/cscope/cscope.files
find $current/* -name '*.java' >> $current/cscope/cscope.files
find /home/song/android-ndk-r6/platforms/android-9/arch-arm/usr/include -type f -name '*.h' >> $current/cscope/cscope.files
find /home/song/android-ndk-r6/platforms/android-9/arch-arm/usr/include -type l -name '*.h' -exec readlink -f {} \; >> $current/cscope/cscope.files
