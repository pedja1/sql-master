LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ls
LOCAL_SRC_FILES := ls.c

include $(BUILD_EXECUTABLE)