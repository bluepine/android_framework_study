# Copyright 2006 The Android Open Source Project

LOCAL_PATH := $(my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= dvz.c zygote.c socket_local_client.c

LOCAL_C_INCLUDES := 

LOCAL_CFLAGS := 

LOCAL_MODULE := dvz 

include $(BUILD_EXECUTABLE)
