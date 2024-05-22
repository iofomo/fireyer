LOCAL_PATH := $(call my-dir)

__SRC_FILES := \
        src/fireyer_main.cpp \
        src/fireyer_native.cpp \

########################### build for .so
include $(CLEAR_VARS)
LOCAL_MODULE        := libfireyer-jni
LOCAL_C_INCLUDES    += $(LOCAL_PATH)/inc
LOCAL_CFLAGS        += -Os -fvisibility=hidden -Wall -Werror
LOCAL_SRC_FILES     += $(__SRC_FILES)
LOCAL_LDLIBS        += -llog -L$(LOCAL_SHARE_LIB_PATH) -lstdc++
ifneq ($(TARGET_ARCH),arm64)
LOCAL_CFLAGS        += -DHAVE_PTHREADS
endif
include $(BUILD_SHARED_LIBRARY)
