package com.ifma.cmpt.fireyer;

import android.app.ActivityManager;
import android.app.AppComponentFactory;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.SigningInfo;
import android.os.Handler;
import android.text.TextUtils;

import com.ifma.cmpt.utils.ActivityThreadUtils;
import com.ifma.cmpt.utils.FileUtils;
import com.ifma.cmpt.utils.Logger;
import com.ifma.cmpt.utils.ReflectUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Brief: say something
 * @Date:   2024.01.08 16:52:52
 */

public class FireyerManager {
    private static final String TAG = "FireyerManager";

    private static boolean classFind(String clsName) {
        try {
            Class<?> cls = Class.forName(clsName);
            if (cls != null) {
                Logger.d(TAG, cls.getName());
            }
        } catch (ClassNotFoundException ignore) {
            return true;
        }
        return false;
    }

    public static boolean checkHiddenAPI() {
        do {
            if (null == ActivityThreadUtils.createAppContext(false)) break;
            // /frameworks/base/core/java/android/app/ActivityThread.java
            if (classFind("android.app.ActivityThread")) break;
            try {
                Method m = ReflectUtils.getDeclaredMethod("android.app.ActivityThread", "currentActivityThread");
                if (null == m) break;
            } catch (Throwable e) {
                break;
            }

            // /android/app/Instrumentation.java
            if (classFind("android.app.Instrumentation")) break;
            try {
                Method m = ReflectUtils.getDeclaredMethod("android.app.Instrumentation", "getFactory", String.class);
                if (null == m) break;
            } catch (Throwable e) {
                break;
            }
            // /libcore/dalvik/src/main/java/dalvik/system/DexPathList.java
            if (classFind("dalvik.system.DexPathList")) break;
            // /frameworks/base/core/java/android/app/LoadedApk.java
            if (classFind("android.app.LoadedApk")) break;
            // /frameworks/base/core/java/android/app/IActivityManager.aidl
            if (classFind("android.app.IActivityManager")) break;
            // /frameworks/base/core/java/android/content/pm/IPackageManager.aidl
            if (classFind("android.content.pm.IPackageManager")) break;
            // TODO add more here ...
            return false;
        } while (false);
        return true;
    }

    public static boolean getOtherMapsLibsBySVC(List<String> maps) {
        String fname = "/proc/" + android.os.Process.myPid() + "/maps";
        String s = FireyerUtils.readFile(fname, true);
        return getMapsLibs(s, "svc", maps);
    }

    public static boolean getOtherMapsLibs(List<String> maps) {
        String fname = "/proc/" + android.os.Process.myPid() + "/maps";
        String s = FileUtils.readFile(fname);
        return getMapsLibs(s, "lib", maps);
    }

    private static boolean getMapsLibs(String s, String tag, List<String> libs) {
        if (TextUtils.isEmpty(s)) return false;
        String[] lines = s.split("\n");
        if (lines.length < 10) return false;
        for (String line : lines) {
            Logger.e(tag + " maps: ", line);
            if (0 < line.indexOf("/data/data/")) {
                libs.add(line);
            } else if (0 < line.indexOf("/data/user/")) {
                libs.add(line);
            }
        }
        return libs.isEmpty();
    }

    public static class IdItem {
        public final int id;
        public final String name;

        public IdItem(int _id, String _name) {
            id = _id;
            name = null == _name ? "" : _name;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IdItem)) {
                return false;
            }
            IdItem p = (IdItem) o;
            return p.id == id;
        }

        @Override
        public int hashCode() { return id; }

        @Override
        public String toString() {
            return "ThreadItem{" + id + " " + name + "}";
        }
    }

    public static Set<IdItem> getOtherProcess(Context ctx) {
        Set<IdItem> set = new HashSet<IdItem>();
        int pid = android.os.Process.myPid();

        ActivityManager am = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (pid == info.pid) continue;
            set.add(new IdItem(info.pid, info.processName));
        }

        int[] pp = FireyerUtils.getPids();
        if (null != pp) {
            for (int p : pp) {
                if (pid == p) continue;
                set.add(new IdItem(p, FireyerUtils.getProceName(p)));
            }
        }
        return set.isEmpty() ? null : set;
    }

    public static Set<IdItem> getJvmThreads() {
        Set<IdItem> set = new HashSet<IdItem>();

        Thread thd;
        Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();
        if (1 < allThreads.size()) {
            for (Map.Entry<Thread, StackTraceElement[]> entry : allThreads.entrySet()) {
                thd = entry.getKey();
                set.add(new IdItem((int) thd.getId(), thd.getName()));
            }
        }
        return set.isEmpty() ? null : set;
    }

    public static Set<IdItem> getNativeThreads() {
        Set<IdItem> set = new HashSet<IdItem>();
        int pid = android.os.Process.myPid();
        int[] tt = FireyerUtils.getThreads();
        if (null != tt) {
            for (int tid : tt) {
                set.add(new IdItem(tid, FireyerUtils.getThreadName(pid, tid)));
            }
        }
        return set.isEmpty() ? null : set;
    }

    public static int getTracerPid() {
        String fname = "/proc/" + android.os.Process.myPid() + "/status";
        String s = FireyerUtils.readFile(fname, true);
        if (null != s) {
            String[] lines = s.split("\n");
            for (String line : lines) {
                if (line.startsWith("TracerPid:")) {
                    line = line.substring("TracerPid:".length()).trim();
                    int pid = Integer.valueOf(line);
                    if (0 != pid) return pid;
                    break;
                }
            }
        }

        fname = "/proc/" + android.os.Process.myPid() + "/task/" + android.os.Process.myPid() + "/status";
        s = FireyerUtils.readFile(fname, true);
        if (null != s) {
            String[] lines = s.split("\n");
            for (String line : lines) {
                if (line.startsWith("TracerPid:")) {
                    line = line.substring("TracerPid:".length()).trim();
                    int pid = Integer.valueOf(line);
                    if (0 != pid) return pid;
                    break;
                }
            }
        }
        return 0;
    }

    public static List<String> getOtherLocalFilesBySVC(Context ctx) {
        String[] ff = (String[])FireyerNative.callNative(FireyerNative.TYPE_FILE_LIST, ctx.getDataDir().getAbsolutePath());
        return getLocalFiles(ff);
    }

    public static List<String> getOtherLocalFiles(Context ctx) {
        String[] ff = new File(ctx.getDataDir().getAbsolutePath()).list();
        return getLocalFiles(ff);
    }

    private static List<String> getLocalFiles(String[] ff) {
        List<String> others = new ArrayList<>();
        if (null != ff) {
            for (String f : ff) {
                if (null == f || TextUtils.equals(f, ".") || TextUtils.equals(f, "..")) continue;
                if (f.startsWith(".")) {
                    others.add(f);
                    Logger.e(TAG, "local file: " + f);
                }
            }
        }
        return others.isEmpty() ? null : others;
    }

    public static boolean checkBinder() throws Throwable {
        if (Proxy.isProxyClass(SigningInfo.CREATOR.getClass())) {
            Logger.e(TAG, "SigningInfo.CREATOR has hooked");
            return false;
        }
        if (Proxy.isProxyClass(ApplicationInfo.CREATOR.getClass())) {
            Logger.e(TAG, "ApplicationInfo.CREATOR has hooked");
            return false;
        }

        Object obj = ReflectUtils.getStaticFieldValue("android.app.ActivityManager", "IActivityManagerSingleton");
        if (null != obj) {
            Object inst = ReflectUtils.getFieldValue(obj, "mInstance");
            if (null != inst && Proxy.isProxyClass(inst.getClass())) {
                Logger.e(TAG, "ActivityManager has hooked");
                return false;// AMS binder has been hooked
            }
        }

        Class<?> cls = Class.forName("android.app.ActivityThread");
        obj = ReflectUtils.getStaticFieldValue(cls, "sPackageManager");
        if (null != obj && Proxy.isProxyClass(obj.getClass())) {
            Logger.e(TAG, "sPackageManager has hooked");
            return false;// PMS binder has been hooked
        }
        // Add more here ...

        return true;
    }

    public static boolean checkApplication(Context ctx, String appClass, String appFacClass, String pkgName) throws Throwable {
        Class<?> cls = Class.forName("android.app.ActivityThread");
        Object objAT = ReflectUtils.getStaticFieldValue(cls, "sCurrentActivityThread");
        Application app = (Application)ReflectUtils.getFieldValue(objAT, "mInitialApplication");
        if (!TextUtils.equals(app.getClass().getName(), appClass)) {
            Logger.e(TAG, "mInitialApplication: " + app.getClass().getName());
            return false;
        }

        Object ins = ReflectUtils.getFieldValue(objAT, "mInstrumentation");
        Method m = ReflectUtils.getDeclaredMethod(ins, "getFactory", String.class);
        AppComponentFactory acf = (AppComponentFactory)m.invoke(ins, ctx.getPackageName());
        if (!TextUtils.equals(acf.getClass().getName(), appFacClass)) {
            Logger.e(TAG, "mInstrumentation::getFactory: " + acf.getClass().getName());
            return false;
        }

        String pkg;
        Logger.e(TAG, "ctx: " + ctx.getClass().getName());
        if (ctx.getClass().getName().endsWith("ContextImpl")) {
            pkg = (String) ReflectUtils.getFieldValue(ctx, "mBasePackageName");
            if (!TextUtils.equals(pkg, pkgName)) {
                Logger.e(TAG, "Context::mBasePackageName fail: " + pkg);
                return false;
            }
            pkg = (String) ReflectUtils.getFieldValue(ctx, "mOpPackageName");
            if (!TextUtils.equals(pkg, pkgName)) {
                Logger.e(TAG, "Context::mOpPackageName fail: " + pkg);
                return false;
            }
        }

        Context c = app.getBaseContext();
        Logger.e(TAG, "app ctx: " + c.getClass().getName());
        if (c.getClass().getName().endsWith("ContextImpl")) {
            pkg = (String) ReflectUtils.getFieldValue(c, "mBasePackageName");
            if (!TextUtils.equals(pkg, pkgName)) {
                Logger.e(TAG, "BaseContext::mBasePackageName fail: " + pkg);
                return false;
            }
            pkg = (String) ReflectUtils.getFieldValue(c, "mOpPackageName");
            if (!TextUtils.equals(pkg, pkgName)) {
                Logger.e(TAG, "BaseContext::mOpPackageName fail: " + pkg);
                return false;
            }
        }

        return true;
    }

    public static boolean checkStack() {
        Map<Thread, StackTraceElement[]> ee = Thread.getAllStackTraces();
        if (null != ee) {
            for (Map.Entry<Thread, StackTraceElement[]> e : ee.entrySet()) {
                Logger.d("stack", "" + e.getKey().getId() + ", " + e.getKey().getName());
                for (StackTraceElement stack : e.getValue()) {
                    Logger.d("stack", stack.toString());
                    if (0 <= stack.getClassName().indexOf("mts")) {// TODO
                        return false;
                    }
                }
            }
        }

        String[] cstacks = (String[])FireyerNative.callNative(FireyerNative.TYPE_GET_STACK, null);
        if (null != cstacks) {
            for (String stack : cstacks) {
                Logger.d(TAG, stack);
                if (0 <= stack.indexOf("mts")) {// TODO
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkClassLoader(Context ctx) {
        //  private Element[] dexElements;
        //  NativeLibraryElement[] nativeLibraryPathElements;
        //  private final List<File> nativeLibraryDirectories;
        Object dexPathList = ReflectUtils.getFieldValue(ctx.getClassLoader(), "pathList");
        if (null == dexPathList) return false;

        Object[] dexElements = (Object[])ReflectUtils.getFieldValue(dexPathList, "dexElements");
        if (null == dexElements || 1 != dexElements.length) {
            return false;
        }
        String str = dexElements[0].toString();
        if (str.indexOf("/data/app") < 0 || str.indexOf("/" + ctx.getPackageName() + "-") < 0) return false;

        Object[] nativeLibraryPathElements = (Object[])ReflectUtils.getFieldValue(dexPathList, "nativeLibraryPathElements");
        if (null != nativeLibraryPathElements) {
            for (Object obj : nativeLibraryPathElements) {
                Object val = ReflectUtils.getFieldValue(obj, "path");
                if (null != val) {
                    str = val.toString();
                    if (0 <= str.indexOf("/data/data") || 0 <= str.indexOf("/data/user")) return false;
                    if (0 <= str.indexOf("/data/app") && str.indexOf("/" + ctx.getPackageName() + "-") < 0) return false;
                }
            }
        }

        List<Object> nativeLibraryDirectories = (List<Object>)ReflectUtils.getFieldValue(dexPathList, "nativeLibraryDirectories");
        if (null != nativeLibraryDirectories) {
            for (Object f : nativeLibraryDirectories) {
                str = f.toString();
                if (str.indexOf("/data/app") < 0 || str.indexOf("/" + ctx.getPackageName() + "-") < 0) return false;
            }
        }
        return true;
    }

    public static boolean checkHCallback() throws Throwable {
        Class<?> cls = Class.forName("android.app.ActivityThread");
        Object obj = ReflectUtils.getStaticFieldValue(cls, "sCurrentActivityThread");
        if (null != obj) {
            Object mh = ReflectUtils.getFieldValue(obj, "mH");
            Object cb = ReflectUtils.getFieldValue(Handler.class, mh, "mCallback");
            if (null != cb) Logger.e(TAG, "h-callback: " + cb);
            return null == cb;
        }
        return true;
    }

    public static class StatInfo {
        public final int uid, gid, mode;
        public final long mtime, ctime, blksize, blocks, inode, size;
        public StatInfo(Object[] args) {
            mtime = Long.valueOf((String)args[0]);
            ctime = Long.valueOf((String)args[1]);
            blksize = Long.valueOf((String)args[2]);
            blocks = Long.valueOf((String)args[3]);
            gid = Integer.valueOf((String)args[4]);
            uid = Integer.valueOf((String)args[5]);
            mode = Integer.valueOf((String)args[6]);
            inode = Long.valueOf((String)args[7]);
            size = Long.valueOf((String)args[8]);
        }
    }

    public static StatInfo getStat(String fileName) {
        Object[] args = FireyerNative.svc_stat(fileName);
        if (null != args) {
            return new StatInfo(args);
        }
        return null;
    }

    public static long getFileSize(String fileName) {
        StatInfo info = getStat(fileName);
        return null != info ? info.size : 0L;
    }

    public static String getPropByPopen(String propName){
        return (String) FireyerNative.callNative(FireyerNative.TYPE_GET_PROP_POPEN ,propName);
    }

    //__system_property_get
    public static String getPropBySPG(String propName){
        return (String) FireyerNative.callNative(FireyerNative.TYPE_GET_PROP_SPG ,propName);
    }

    //__system_property_read_callback
    public static String getPropBySPRC(String propName){
        return (String) FireyerNative.callNative(FireyerNative.TYPE_GET_PROP_SPRC ,propName);
    }

}
