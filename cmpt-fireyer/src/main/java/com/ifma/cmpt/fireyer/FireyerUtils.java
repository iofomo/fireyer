package com.ifma.cmpt.fireyer;

import android.text.TextUtils;

import com.ifma.cmpt.utils.CipherUtils;

import java.io.Closeable;
import java.io.File;
import java.security.MessageDigest;

/**
 * @Brief: say something
 * @Date:   2024.01.08 16:52:52
 */

public class FireyerUtils {
    private static final String TAG = "FireyerManager";

    public static String md5File(String file, boolean checkFileName) {
        int fd = -1;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fd = FireyerNative.svc_open(file, true);
            if (fd < 0) return null;

            if (checkFileName && !TextUtils.equals(file, FireyerNative.svc_readlink(fd))) {
                return null;
            }

            int len;
            byte[] buffer = new byte[8192];
            while (0 < (len = FireyerNative.svc_read(fd, buffer, buffer.length))) {
                md.update(buffer, 0, len);
            }
            return CipherUtils.bytesToHexString(md.digest());
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (0 <= fd) FireyerNative.svc_close(fd);
        }
        return null;
    }

    public static String readFile(String file, boolean checkFileName) {
        int fd = -1;
        try {
            StringBuilder sb = new StringBuilder();
            fd = FireyerNative.svc_open(file, true);
            if (fd < 0) return null;

            if (checkFileName && !TextUtils.equals(file, FireyerNative.svc_readlink(fd))) {
                return null;
            }

            int len;
            byte[] buffer = new byte[8192];
            while (0 < (len = FireyerNative.svc_read(fd, buffer, buffer.length))) {
                sb.append(new String(buffer, 0, len));
            }
            return sb.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (0 <= fd) FireyerNative.svc_close(fd);
        }
        return null;
    }

    public static int[] getThreads() {
        return (int[])FireyerNative.callNative(FireyerNative.TYPE_GET_THREAD, null);
    }

    public static String getThreadName(int pid, int tid) {
        String s = readFile("/proc/" + pid + "/task/" + tid + "/status", true);
        if (TextUtils.isEmpty(s)) return null;
        String[] lines = s.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.startsWith("Name:")) continue;
            return line.substring("Name:".length()).trim();
        }
        return null;
    }

    public static int[] getPids() {
        return (int[])FireyerNative.callNative(FireyerNative.TYPE_GET_PROC, android.os.Process.myUid());
    }

    public static String getProceName(int pid) {
        String s = readFile("/proc/" + pid + "/cmdline", true);
        if (!TextUtils.isEmpty(s)) return s.trim();

        s = readFile("/proc/" + pid + "/status", true);
        String[] items = s.split("\n");
        for (String line : items) {
            if (line.startsWith("Name:")) {
                return line.substring(5).trim();
            }
        }
        return null;
    }

    public static void closeQuietly(Closeable closeable){
        if (closeable != null){
            try {
                closeable.close();
            }catch (Throwable t){}
        }
    }
}
