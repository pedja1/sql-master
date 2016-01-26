LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ls
LOCAL_SRC_FILES := ls.c

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE    := sqlite_verify
LOCAL_SRC_FILES := sqlite_verify_main.c sqlite_verify.c

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES += $(LOCAL_PATH)/sqlite3
LOCAL_C_INCLUDES += $(LOCAL_PATH)

LOCAL_MODULE    := sqlite_cmd
LOCAL_SRC_FILES := sqlite_cmd.c sqlite_verify.c sqlite3/sqlite3.c

include $(BUILD_EXECUTABLE)