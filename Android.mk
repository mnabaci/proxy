LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#LOCAL_MODULE_TAGS := eng

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += src/com/android/proxy/IProxyService.aidl

LOCAL_PACKAGE_NAME := Proxy

# can not be platform
LOCAL_CERTIFICATE := shared

LOCAL_STATIC_JAVA_LIBRARIES := com.android.proxy.proxyservice

# specify the jni lib, only for android with version greater than 1.5. not for OMS

include $(BUILD_PACKAGE)


include $(CLEAR_VARS)
LOCAL_SRC_FILES := src/com/android/proxy/IProxyService.aidl
LOCAL_MODULE := com.android.proxy.proxyservice
LOCAL_CERTIFICATE := shared

include $(BUILD_STATIC_JAVA_LIBRARY)



# Use the following include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))

