#!/bin/sh
#set -x
current=`pwd`
rm -rf cscope
mkdir cscope
find $current/* -name '*.c' > $current/cscope/cscope.files
find $current/* -name '*.cpp' > $current/cscope/cscope.files
find $current/* -name '*.h' >> $current/cscope/cscope.files
find $current/* -name '*.java' >> $current/cscope/cscope.files
