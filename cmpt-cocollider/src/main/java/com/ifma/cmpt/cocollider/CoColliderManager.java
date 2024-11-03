package com.ifma.cmpt.cocollider;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.ifma.cmpt.utils.CmnInitor;
import com.ifma.cmpt.utils.FileUtils;
import com.ifma.cmpt.utils.ReflectUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CoColliderManager {
    private static final String TAG = "CoColliderManager";
    public static final String KEY_IN = "key-in";
    public static final String KEY_OUT = "key-out";

    public static void handleCallEvent(Context ctx, Bundle inData, Bundle replayData) throws Throwable {
        String fileIn = inData.getString(KEY_IN);
        File fileOut = new File(ctx.getExternalCacheDir().getParent(), "cocollider-run.txt");

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(fileIn);
            os = new FileOutputStream(fileOut);
            if (doParse(is, os)) {
                replayData.putString(KEY_OUT, fileOut.getAbsolutePath());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeQuietly(is);
            FileUtils.closeQuietly(os);
        }
    }

    private static byte[] sOKBytes = "[OK], ".getBytes();
    private static byte[] sFailBytes = "[Fail]".getBytes();
    private static int CLASS_START = 1;
    private static int NATIVE_START = 2;

    public static boolean doParse(InputStream is, OutputStream os) throws Throwable {
        int status = 0;
        String line, lastClassName = null;
        Class<?> lastClass = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (null != (line = reader.readLine())) {
            os.write(line.getBytes());
            os.write('\n');
            if (line.startsWith("~")) {// native
                status = NATIVE_START;
                lastClassName = line.substring(1).trim();
                if (0L != CmnInitor.getLibSym(lastClassName, null)) {
                    os.write(sOKBytes);
                    os.write(lastClass.toGenericString().getBytes());
                    os.write('\n');
                } else {
                    os.write(sFailBytes);
                    os.write('\n');
                }
            } else if (line.startsWith("=")) {// class
                status = CLASS_START;
                lastClassName = line.substring(1).trim();
                lastClass = ReflectUtils.findClass(lastClassName);
                if (null != lastClass) {
                    os.write(sOKBytes);
                    os.write(lastClass.toGenericString().getBytes());
                    os.write('\n');
                } else {
                    os.write(sFailBytes);
                    os.write('\n');
                }
            } else if (line.startsWith("+")) {// method
                if (status == CLASS_START) {
                    List<String> mm = reflectMethod(lastClass, line.substring(1).trim());
                    if (null == mm) {
                        os.write(sFailBytes);
                        os.write('\n');
                    } else {
                        for (String m : mm) {
                            os.write(m.getBytes());
                            os.write('\n');
                        }
                    }
                } else {
                    if (0L != CmnInitor.getLibSym(lastClassName, line.substring(1).trim())) {
                        os.write(sOKBytes);
                        os.write(lastClassName.getBytes());
                        os.write('\n');
                    } else {
                        os.write(sFailBytes);
                        os.write('\n');
                    }
                }
            } else if (line.startsWith("-")) {// field
                if (status == CLASS_START) {
                    List<String> ff = reflectField(lastClass, line.substring(1).trim());
                    if (null == ff) {
                        os.write(sFailBytes);
                        os.write('\n');
                    } else {
                        for (String f : ff) {
                            os.write(f.getBytes());
                            os.write('\n');
                        }
                    }
                } else {
                    if (0L != CmnInitor.getLibSym(lastClassName, line.substring(1).trim())) {
                        os.write(sOKBytes);
                        os.write(lastClassName.getBytes());
                        os.write('\n');
                    } else {
                        os.write(sFailBytes);
                        os.write('\n');
                    }
                }
            }
        }
        return false;
    }

    private static List<String> reflectMethod(Class<?> cls, String method) {
        if (null == cls) return null;

        List<String> ss = new ArrayList<>();
        try {
            Method[] mm = cls.getDeclaredMethods();
            for (Method m : mm) {
                if (!m.isAccessible()) m.setAccessible(true);
                if (TextUtils.equals(method, "*") || TextUtils.equals(method, m.getName())) {
                    ss.add(m.toGenericString());
                }
            }
            return 0 < ss.size() ? ss : null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<String> reflectField(Class<?> cls, String field) {
        if (null == cls) return null;

        List<String> ss = new ArrayList<>();
        try {
            if (TextUtils.equals(field, "*")) {
                Field[] ff = cls.getDeclaredFields();
                for (Field f : ff) {
                    if (!f.isAccessible()) f.setAccessible(true);
                    ss.add(f.toGenericString());
                }
            } else {
                Field f = cls.getDeclaredField(field);
                if (null == f) return null;
                if (!f.isAccessible()) f.setAccessible(true);
                ss.add(f.toGenericString());
            }
            return 0 < ss.size() ? ss : null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
