#include <fcntl.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <unistd.h>
#include "assembly.h"
#include "fireyer_native.h"

#define MEM_FREE(p) if(p){ free(p); p=0; }
// ----------------------------------------------------------------------------------------------------------------
static bool utils_is_number_string(const char* str) {
    if ('\0' == (*str)) return false;
    while (*str) {
        if (!isdigit(*str ++)) return false;
    }
    return true;
}

jstring jni_stringTojstring(JNIEnv* env, const char* pat) {
    if (env && pat) {
        return env->NewStringUTF(pat);
    }
    return NULL;
}

char* jni_jstringTostring(JNIEnv* env, jstring jstr) {
    char* utf = NULL;
    if (env && jstr) {
        const char* str = env->GetStringUTFChars(jstr, NULL);
        if (str) {
            utf = strdup(str);
            env->ReleaseStringUTFChars(jstr, str);
        }
    }
    return utf;
}

void* jni_jbytearray_new(JNIEnv* env, jbyteArray jbyteArr, int off, int len) {
    if (!env || !jbyteArr) return NULL;
    jbyte* data = (jbyte *) malloc(len);
    if (data) {
        env->GetByteArrayRegion(jbyteArr, off, len, data);
    }
    return data;
}

jintArray jni_create_int_array(JNIEnv* env, const int* args, int argCnt) {
    if (!env || !args || argCnt <= 0) return NULL;
    jintArray jints = env->NewIntArray(argCnt);
    if (jints) {
        env->SetIntArrayRegion(jints, 0, argCnt, args);
    }
    return jints;
}

jobjectArray jni_string_array_new(JNIEnv* env, const char** str, int strCnt) {
    if (!env || !str || strCnt <= 0) return NULL;
    jclass jcls = env->FindClass("java/lang/String");
    jobjectArray jarr = env->NewObjectArray(strCnt, jcls, NULL);
    if (!jarr) return NULL;

    jstring jstr;
    for (int i=0; i<strCnt; ++i) {
        jstr = jni_stringTojstring(env, str[i]);
        env->SetObjectArrayElement(jarr, i, jstr);
    }
    return jarr;
}

jobjectArray jni_string_array_new_for_jstring(JNIEnv* env, int argCnt, ...) {
    if (!env ||argCnt <= 0) return NULL;
    jclass jcls = env->FindClass("java/lang/String");
    jobjectArray jarr = env->NewObjectArray(argCnt, jcls, NULL);
    if (!jarr) return NULL;

    jstring jstr;
    va_list arg_ptr;
    va_start(arg_ptr, argCnt);
    for (int i=0; i<argCnt; ++i) {
        jstr = va_arg(arg_ptr, jstring);
        env->SetObjectArrayElement(jarr, i, jstr);
    }
    va_end(arg_ptr);
    return jarr;
}

int FireyerNative::svc_open(JNIEnv* env, jclass thiz, jstring jname, jboolean jmode) {
    int fd = -1;
    char* fname = jni_jstringTostring(env, jname);
    if (fname) {
        if (jmode) {// read
            fd = SVC_SYSCALL(SVC_OPENAT, 3, AT_FDCWD, fname, O_RDONLY);
        } else {// write
            fd = SVC_SYSCALL(SVC_OPENAT, 4, AT_FDCWD, fname, O_WRONLY | O_CREAT | O_TRUNC, 0644);
        }
        free(fname);
    }
    return fd;
}

int FireyerNative::svc_read(JNIEnv* env, jclass thiz, jint fd, jbyteArray buffer, int bufferSize) {
    int ret = -1;
    void* data = malloc(bufferSize);
    if (data) {
        ret = SVC_SYSCALL(SVC_READ, 3, fd, data, bufferSize);
        if (0 < ret) {
            env->SetByteArrayRegion(buffer, 0, ret, reinterpret_cast<const jbyte*>(data));
        }
        free(data);
    }
    return ret;
}

int FireyerNative::svc_write(JNIEnv* env, jclass thiz, jint fd, jbyteArray buffer, int bufferSize) {
    int ret = -1;
    void* data = jni_jbytearray_new(env, buffer, 0, bufferSize);
    if (data) {
        ret = SVC_SYSCALL(SVC_WRITE, 3, fd, data, bufferSize);
        free(data);
    }
    return ret;
}

int FireyerNative::svc_close(JNIEnv* env, jclass thiz, jint fd) {
    return SVC_SYSCALL(SVC_CLOSE, 1, fd);
}

jstring FireyerNative::svc_readlink(JNIEnv* env, jclass thiz, jint fd) {
    // int dirfd, const char *pathname, char *buf, size_t bufsiz
    char path[64];
    sprintf(path, "/proc/%d/fd/%d", getpid(), fd);
    char buff[512];
    int ret = SVC_SYSCALL(SVC_READLINKAT, 4, AT_FDCWD, path, buff, sizeof(buff));
    if (0 < ret) {
        buff[ret] = '\0';
        return jni_stringTojstring(env, buff);
    }
    return nullptr;
}

jobjectArray FireyerNative::svc_stat(JNIEnv* env, jclass thiz, jstring jname) {
    jobjectArray jret = NULL;
    struct stat file_stat;
    char* filename = jni_jstringTostring(env, jname);
    if (filename) {
        if (0 == SVC_SYSCALL(SVC_FSTATAT, 4, AT_FDCWD, filename, &file_stat, 0)) {
            char buffer[256];
            long long ts = file_stat.st_mtim.tv_sec * 1000LL + file_stat.st_mtim.tv_nsec / 1e6;
            sprintf(buffer, "%lld", ts);
            jstring mtime = jni_stringTojstring(env, buffer);
            ts = file_stat.st_ctim.tv_sec * 1000LL + file_stat.st_ctim.tv_nsec / 1e6;
            sprintf(buffer, "%lld", ts);
            jstring ctime = jni_stringTojstring(env, buffer);

#if defined(__aarch64__)
            sprintf(buffer, "%d", file_stat.st_blksize);
            jstring blksize = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%ld", file_stat.st_blocks);
            jstring blocks = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%d", file_stat.st_gid);
            jstring gid = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%ld", file_stat.st_ino);
            jstring inode = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%d", file_stat.st_mode);
            jstring mode = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%ld", file_stat.st_size);
            jstring size = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%d", file_stat.st_uid);
            jstring uid = jni_stringTojstring(env, buffer);
#else
            sprintf(buffer, "%ld", file_stat.st_blksize);
            jstring blksize = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%lld", file_stat.st_blocks);
            jstring blocks = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%d", file_stat.st_gid);
            jstring gid = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%lld", file_stat.st_ino);
            jstring inode = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%d", file_stat.st_mode);
            jstring mode = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%lld", file_stat.st_size);
            jstring size = jni_stringTojstring(env, buffer);
            sprintf(buffer, "%d", file_stat.st_uid);
            jstring uid = jni_stringTojstring(env, buffer);
#endif
            jret = jni_string_array_new_for_jstring(env, 9,
                mtime, ctime, blksize, blocks, gid, uid, inode, mode, size
            );
        }
        free(filename);
    }
    return jret;
}

static uid_t get_file_uid(const char* filename) {
    struct stat file_stat;
    if (SVC_SYSCALL(SVC_FSTATAT, 4, AT_FDCWD, filename, &file_stat, 0) == 0) {
        return file_stat.st_uid;
    }
    return -1;
}

#define BUF_SIZE    1024
static int svc_dir_list_self_pids(int* pids, int count) {
    int nread;
    char buf[BUF_SIZE];
    char fileName[128];
    struct linux_dirent *d;
    int bpos;

    int fd = open("/proc", O_RDONLY | O_DIRECTORY);
    if (fd == -1) return -1;

    int index = 0;
    uid_t uid = getuid();

    strcpy(fileName, "/proc/");
    while (true) {
        nread = SVC_SYSCALL(SVC_GETDENTS, 3, fd, buf, BUF_SIZE);
        if (nread <= 0) break;

        for (bpos = 0; bpos < nread;) {
            d = (struct linux_dirent *) (buf + bpos);
//            printf("%8ld  %3d         %4d  %10lld  %s\n", d->d_ino, d->d_type, d->d_reclen, (long long) d->d_off, d->d_name);
            if (index < count) {
                if (utils_is_number_string(d->d_name)) {
                    strcpy(fileName + 6, d->d_name);
                    if (uid == get_file_uid(fileName)) {
                        pids[index++] = atoi(d->d_name);
                    }
                }
            } else {
                break;
            }
            bpos += d->d_reclen;
        }
    }
    close(fd);
    return index;
}

static int svc_dir_list_tids(const char* path, int* files, int count) {
    int nread;
    char buf[BUF_SIZE];
    struct linux_dirent *d;
    int bpos;

    int fd = open(path, O_RDONLY | O_DIRECTORY);
    if (fd == -1) return -1;

    int index = 0;
    while (true) {
        nread = SVC_SYSCALL(SVC_GETDENTS, 3, fd, buf, BUF_SIZE);
        if (nread <= 0) break;

        for (bpos = 0; bpos < nread;) {
            d = (struct linux_dirent *) (buf + bpos);
//            printf("%8ld  %3d         %4d  %10lld  %s\n", d->d_ino, d->d_type, d->d_reclen, (long long) d->d_off, d->d_name);
            if (index < count) {
                if (utils_is_number_string(d->d_name)) {
                    files[index++] = atoi(d->d_name);
                }
            } else {
                break;
            }
            bpos += d->d_reclen;
        }
    }
    close(fd);
    return index;
}

static int svc_dir_list_name(const char* path, char** files, int count) {
    int nread;
    char buf[BUF_SIZE];
    struct linux_dirent *d;
    int bpos;

    int fd = open(path, O_RDONLY | O_DIRECTORY);
    if (fd == -1) return -1;

    int index = 0;
    while (true) {
        nread = SVC_SYSCALL(SVC_GETDENTS, 3, fd, buf, BUF_SIZE);
        if (nread <= 0) break;

        for (bpos = 0; bpos < nread;) {
            d = (struct linux_dirent *) (buf + bpos);
//            printf("%8ld  %3d         %4d  %10lld  %s\n", d->d_ino, d->d_type, d->d_reclen, (long long) d->d_off, d->d_name);
            if (index < count) {
                files[index++] = strdup(d->d_name);
            } else {
                break;
            }
            bpos += d->d_reclen;
        }
    }
    close(fd);
    return index;
}

jobject FireyerNative::handeJavaCall(JNIEnv* env, jclass thiz, jint jtype, jobject jobj) {
    jobject jret = NULL;
    switch (jtype) {
    case TYPE_GET_THREAD:
    {
        int* files = (int*)malloc(sizeof(int) * 1000);
        if (files) {
            char path[64];
            sprintf(path, "/proc/%d/task", getpid());
            int cnt = svc_dir_list_tids(path, files, 1000);
            if (0 < cnt) {
                jret = jni_create_int_array(env, files, cnt);
            }
        }
        MEM_FREE(files)
    }   break;
    case TYPE_GET_PROC:
    {
        int* files = (int*)malloc(sizeof(int) * 1000);
        if (files) {
            int cnt = svc_dir_list_self_pids(files, 1000);
            if (0 < cnt) {
                jret = jni_create_int_array(env, files, cnt);
            }
        }
    }   break;
    case TYPE_FILE_LIST:
    {
        char** files = (char**)malloc(sizeof(void*) * 1000);
        char* path = jni_jstringTostring(env, (jstring)jobj);
        if (files && path) {
            int cnt = svc_dir_list_name(path, files, 1000);
            if (0 < cnt) {
                jret = jni_string_array_new(env, (const char**)files, cnt);
            }
        }
        MEM_FREE(files)
        MEM_FREE(path)
    }   break;
    case TYPE_GET_STACK:
    {

    }   break;
    default: break;
    }
    return jret;
}
