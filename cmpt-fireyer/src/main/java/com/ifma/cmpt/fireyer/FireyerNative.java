package com.ifma.cmpt.fireyer;

/**
 * @Brief: say something
 * @Date:   2024.01.08 16:52:52
 */

public final class FireyerNative {
    public static final int TYPE_GET_THREAD = 1;
    public static final int TYPE_GET_PROC   = 2;
    public static final int TYPE_FILE_LIST  = 3;
    public static final int TYPE_GET_STACK  = 4;

    static {
        try {
            System.loadLibrary("fireyer-jni");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static native int svc_open(String name, boolean isRead);
    public static native int svc_read(int fd, byte[] buffer, int bufferSize);
    public static native int svc_write(int fd, byte[] buffer, int bufferSize);
    public static native int svc_close(int fd);

    /**
     * [0~8]: mtime, ctime, blksize, blocks, gid, uid, inode, mode, size
     * **/
    public static native Object[] svc_stat(String name);
    public static native String svc_readlink(int fd);

    public static native Object callNative(int type, Object arg);
}
