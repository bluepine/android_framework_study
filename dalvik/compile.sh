#!/bin/bash
javac Foo.java
dx --dex --output=foo.jar Foo.class
