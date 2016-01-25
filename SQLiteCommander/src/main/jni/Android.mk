LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ls
LOCAL_SRC_FILES := ls.c

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE    := sqlite_verify
LOCAL_SRC_FILES := sqlite_verify.c

include $(BUILD_EXECUTABLE)