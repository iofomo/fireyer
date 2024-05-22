#include <sys/stat.h>
#include "fireyer_native.h"

#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

// ------------------------------------------------------------------------------------------------------------------------------------------------------
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    if (NULL == vm) {
        return -1;
    }

    /* Check GetEnv return value */
    JNIEnv *env = NULL;
    JNIEnv **ppenv = &env;
    int ret = vm->GetEnv((void **) ppenv, JNI_VERSION_1_6);
    if (ret != JNI_OK) {
        return -2;
    }

    if (NULL == env) {
        return -3;
    }

    jclass clazz = env->FindClass("com/ifma/cmpt/fireyer/FireyerNative");
    if (NULL == clazz) {
        return -4;
    }

    JNINativeMethod methods[] = {
        {"svc_open", "(Ljava/lang/String;Z)I", (void *) FireyerNative::svc_open},
        {"svc_read", "(I[BI)I", (void *) FireyerNative::svc_read},
        {"svc_write", "(I[BI)I", (void *) FireyerNative::svc_write},
        {"svc_close", "(I)I", (void *) FireyerNative::svc_close},
        {"svc_stat", "(Ljava/lang/String;)[Ljava/lang/Object;", (void *) FireyerNative::svc_stat},
        {"svc_readlink", "(I)Ljava/lang/String;", (void *) FireyerNative::svc_readlink},
        {"callNative", "(ILjava/lang/Object;)Ljava/lang/Object;", (void *) FireyerNative::handeJavaCall},
    };
    ret = env->RegisterNatives(clazz, methods, NELEM(methods));
    if (ret < 0) {
        return -5;
    }

    return JNI_VERSION_1_6;
}