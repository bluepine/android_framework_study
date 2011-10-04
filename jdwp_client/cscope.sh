#!/bin/sh
#set -x
current=`pwd`
rm -rf cscope
mkdir cscope
find $current/* -name '*.java' > $current/cscope/cscope.files


