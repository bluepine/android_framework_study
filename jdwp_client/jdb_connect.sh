#!/bin/sh
jdb -J-Duser.home=. -connect com.sun.jdi.SocketAttach:hostname=localhost,port=$1
