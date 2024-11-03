#ifndef __FIREYER_NATIVE_H__
#define __FIREYER_NATIVE_H__

#include <jni.h>

enum {
    TYPE_GET_THREAD = 1,
    TYPE_GET_PROC = 2,
    TYPE_FILE_LIST = 3,
    TYPE_GET_STACK = 4,
    TYPE_GET_PROP_POPEN   = 5,
    TYPE_GET_PROP_SPG     = 6,
    TYPE_GET_PROP_SPRC    = 7,
};

class FireyerNative {
public:
    static int svc_open(JNIEnv* env, jclass thiz, jstring jname, jboolean jmode);
    static int svc_read(JNIEnv* env, jclass thiz, jint fd, jbyteArray buffer, int bufferSize);
    static int svc_write(JNIEnv* env, jclass thiz, jint fd, jbyteArray buffer, int bufferSize);
    static int svc_close(JNIEnv* env, jclass thiz, jint fd);
    static jobjectArray svc_stat(JNIEnv* env, jclass thiz, jstring jname);
    static jstring svc_readlink(JNIEnv* env, jclass thiz, jint fd);

    static jobject handeJavaCall(JNIEnv* env, jclass thiz, jint jtype, jobject jobj);
};

#endif// end of __FIREYER_NATIVE_H__
