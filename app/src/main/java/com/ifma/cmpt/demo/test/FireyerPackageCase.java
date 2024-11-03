/**
 * @brief: test case
 *      only the method which is public and start with "test" will be run, such as:
 *      public void testXXX() {
 *          // TODO something ...
 *      }
 * */
package com.ifma.cmpt.demo.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.ifma.cmpt.demo.fireyer.BuildConfig;
import com.ifma.cmpt.demo.fireyer.R;
import com.ifma.cmpt.demo.module.ClipboadData;
import com.ifma.cmpt.fireyer.FireyerManager;
import com.ifma.cmpt.fireyer.FireyerUtils;
import com.ifma.cmpt.testin.env.TstEnv;
import com.ifma.cmpt.testin.module.TstCaseBase;
import com.ifma.cmpt.testin.module.TstRunner;
import com.ifma.cmpt.utils.ActivityThreadUtils;
import com.ifma.cmpt.utils.CipherUtils;
import com.ifma.cmpt.utils.FileUtils;
import com.ifma.cmpt.utils.Logger;
import com.ifma.cmpt.utils.MD5Utils;
import com.ifma.cmpt.utils.OSUtils;
import com.ifma.cmpt.utils.ReflectUtils;

import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

public class FireyerPackageCase extends TstCaseBase {
    private static final String TAG = "FireyerPackageCase";

    PackageManager mPM;
    PackageInfo mPackageInfo;
    protected void setUp() throws Exception {
        super.setUp();
        mPM = getContext().getPackageManager();
        mPackageInfo = mPM.getPackageInfo(getContext().getPackageName(),
        PackageManager.GET_META_DATA | PackageManager.GET_ACTIVITIES |
            PackageManager.GET_SERVICES|PackageManager.GET_PROVIDERS|
            PackageManager.GET_RECEIVERS|PackageManager.GET_PERMISSIONS|
            PackageManager.GET_SIGNING_CERTIFICATES|PackageManager.GET_INTENT_FILTERS
        );
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static void dumpPackageInfo(ApplicationInfo aInfo) {
        final String[] ss = new String[] {
            String.valueOf(aInfo.flags)
        };
        Logger.e(TAG, String.format("flags: 0x%x", aInfo.flags));
        ClipboadData.set(ClipboadData.TYPE_Package_Info, ss);
    }

    public static int getAppInfoFlags() {
        final List<String> ss = ClipboadData.get(ClipboadData.TYPE_Package_Info);
        if (null != ss && 0 < ss.size()) {
            return Integer.valueOf(ss.get(0));
        }
        return 0;
    }

    public static boolean doCheckMainMetaData(Bundle metaData) {
        if (null == metaData) return false;
        if (!TextUtils.equals("test_main_value", metaData.getString("test_main_value"))) return false;
        if (R.string.test_main_string != metaData.getInt("test_main_string", 0)) return false;// resource
        if (R.bool.test_main_true != metaData.getInt("test_main_true", 0)) return false;// resource
        if (R.bool.test_main_false != metaData.getInt("test_main_false", 0)) return false;// resource
        if (R.integer.test_main_int1 != metaData.getInt("test_main_int1", 0)) return false;// resource
        if (R.integer.test_main_int2 != metaData.getInt("test_main_int2", 0)) return false;// resource
        return true;
    }

    public static boolean doCheckSubMetaData(Bundle metaData) {
        if (null == metaData) return false;
        if (!TextUtils.equals("test_sub_value", metaData.getString("test_sub_value"))) return false;
        if (R.string.test_sub_string != metaData.getInt("test_sub_string", 0)) return false;// resource
        if (R.bool.test_sub_true != metaData.getInt("test_sub_true", 0)) return false;// resource
        if (R.bool.test_sub_false != metaData.getInt("test_sub_false", 0)) return false;// resource
        if (R.integer.test_sub_int1 != metaData.getInt("test_sub_int1", 0)) return false;// resource
        if (R.integer.test_sub_int2 != metaData.getInt("test_sub_int2", 0)) return false;// resource
        return true;
    }

    public static void doCheckApplicationInfo(String tag, ApplicationInfo ai) {
        TstRunner.print(tag + "application extractNativeLibs", 0 == (ai.flags & ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS));
        final int flags = getAppInfoFlags();
        if (flags == ai.flags) {
            TstRunner.print(tag + "application flags", true);
        } else {
            TstRunner.print(tag + String.format("application flags: 0x%x != 0x%x", flags, ai.flags), false);
        }

        TstRunner.print(tag + "application className", TextUtils.equals("com.ifma.cmpt.demo.FakeApplication", ai.className)||TextUtils.isEmpty(ai.className));
        TstRunner.print(tag + "application packageName", TextUtils.equals(FireyerCaseConsts.PACKAGE_NAME, ai.packageName));
        TstRunner.print(tag + "application name", TextUtils.equals("com.ifma.cmpt.demo.FakeApplication", ai.name));
        if (TextUtils.equals("com.ifma.cmpt.demo.DemoComponentFactory", ai.appComponentFactory)) {
            TstRunner.print(tag + "application acf", true);
        } else {
            TstRunner.print(tag + "application acf: " + ai.appComponentFactory, false);
        }
        TstRunner.print(tag + "application theme", ai.theme == R.style.AppBaseTheme);
        TstRunner.print(tag + "application labelRes", ai.labelRes == R.string.app_name);
        if (null != ai.sourceDir) {
            TstRunner.print(tag + "application sourceDir", ai.sourceDir.startsWith("/data/app") && 0 < ai.sourceDir.indexOf(FireyerCaseConsts.PACKAGE_NAME));
        } else {
            TstRunner.print(tag + "application sourceDir null", TextUtils.equals("local ", tag));
        }
        if (null != ai.publicSourceDir) {
            TstRunner.print(tag + "application publicSourceDir", ai.publicSourceDir.startsWith("/data/app") && 0 < ai.publicSourceDir.indexOf(FireyerCaseConsts.PACKAGE_NAME));
        } else {
            TstRunner.print(tag + "application publicSourceDir null", TextUtils.equals("local ", tag));
        }
        if (TextUtils.isEmpty(sSourceApk)) {
            final List<String> ss = ClipboadData.get(ClipboadData.TYPE_Source_APK);
            if (null != ss && 2 < ss.size()) {
                sSourceApk = ss.get(2);
            }
        }
        Logger.d(TAG, tag + "sSourceApk: " + sSourceApk);
        final String srcApk = OSUtils.getApkSourceFile(ai);
        Logger.d(TAG, tag + "srcApk: " + srcApk);
        TstRunner.print(tag + "application source apk", null == sSourceApk || ((null == srcApk) && TextUtils.equals("local ", tag)) || TextUtils.equals(sSourceApk, srcApk));

        TstRunner.print(tag + "application extract_native_lib", 0 == (ai.flags & ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS));
        if (null != ai.nativeLibraryDir) {
            TstRunner.print(tag + "application libDir", ai.nativeLibraryDir.startsWith("/data/app") && 0 < ai.nativeLibraryDir.indexOf(FireyerCaseConsts.PACKAGE_NAME));
            TstRunner.print(tag + "application lib common", !new File(ai.nativeLibraryDir, "libifmacommon-jni.so").exists());
            TstRunner.print(tag + "application lib fireyer", !new File(ai.nativeLibraryDir, "libfireyer-jni.so").exists());
        }
        TstRunner.print(tag + "application processName", TextUtils.equals(FireyerCaseConsts.PACKAGE_NAME, ai.processName) || TextUtils.isEmpty(ai.processName));
        TstRunner.print(tag + "application icon", ai.icon == R.drawable.ic_launcher);
        TstRunner.print(tag + "application targetSdkVersion", ai.targetSdkVersion == 30);
    }

    public boolean doCheckSignature(SigningInfo info) {
        Signature[] ss = info.getApkContentsSigners();
        if (null == ss || ss.length != 1) return false;
        Logger.d(TAG, String.format("0x%x", ss[0].hashCode()));
        String sha1 = MD5Utils.sha1(ss[0].toByteArray(), true);
        return TextUtils.equals(sha1, "9f1c0a0ac5df0f801f085d5011c5730283994a0a");
    }

    void doCheckApplication(String tag, PackageInfo pi) {
        Bundle metaData = pi.applicationInfo.metaData;
        TstRunner.print(tag + "application count metadata", null != metaData && metaData.size() == 13);
        TstRunner.print(tag + "application main metadata", doCheckMainMetaData(metaData));
        TstRunner.print(tag + "application sub metadata", doCheckSubMetaData(metaData));

        TstRunner.print(tag + "permission", pi.permissions.length == 3);
        for (PermissionInfo pinfo : pi.permissions) {
            if (TextUtils.equals(pinfo.name, "com.ifma.cmpt.demo.permission")) continue;
            if (TextUtils.equals(pinfo.name, "com.ifma.cmpt.demo.readPermission")) continue;
            if (TextUtils.equals(pinfo.name, "com.ifma.cmpt.demo.writePermission")) continue;
            TstRunner.print(tag + "permission fail: " + pinfo.name, false);
        }
        TstRunner.print(tag + "requestedPermissions", pi.requestedPermissions.length == 2);
        for (String permission : pi.requestedPermissions) {
            if (TextUtils.equals(permission, "com.ifma.cmpt.demo.permission")) continue;
            if (TextUtils.equals(permission, "android.permission.INTERNET")) continue;
            TstRunner.print(tag + "requested permission fail: " + permission, false);
        }

        doCheckApplicationInfo(tag, pi.applicationInfo);
        TstRunner.print(tag + "version", pi.getLongVersionCode() == BuildConfig.VERSION_CODE && TextUtils.equals(pi.versionName, BuildConfig.VERSION_NAME));
        TstRunner.print(tag + "sharedUser", pi.sharedUserLabel == 0 && TextUtils.isEmpty(pi.sharedUserId));
    }

    public void testAndroidManifest() throws Exception {
        TstRunner.print("package name", FireyerCaseConsts.PACKAGE_NAME.equals(getContext().getPackageName()));
        doCheckApplication("binder ", mPackageInfo);
        doTestComponent(mPackageInfo, true);
        ApplicationInfo ai = getContext().getApplicationContext().getApplicationInfo();
        doCheckApplicationInfo("AppCxt ", ai);
    }

    public static void doTestActivityInfo(String tag, ActivityInfo info, boolean checkMetaData) {
        if (info == null) return;
        if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.main.MainActivity")) {
            if (checkMetaData) {
                TstRunner.print(tag + "activity main metaData", doCheckMainMetaData(info.metaData));
            }
            TstRunner.print(tag + "activity main theme: " + info.theme, info.theme == R.style.AppTheme);
        } else if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.sub.SubActivity")) {
            if (checkMetaData) {
                TstRunner.print(tag + "activity sub metaData", doCheckSubMetaData(info.metaData));
            }
            TstRunner.print(tag + "activity sub taskAffinity: ", TextUtils.equals(info.taskAffinity, ".tasktest"));
            TstRunner.print(tag + "activity sub drawable", info.icon == R.drawable.activity);
        } else if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.main.ConsoleActivity")) {
            if (checkMetaData) {
                TstRunner.print(tag + "activity console metaData", null == info.metaData || info.metaData.isEmpty());
            }
        } else if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.sub.DocumentReceiverActivity")) {
            TstRunner.print(tag + "activity document taskAffinity: " + info.taskAffinity, TextUtils.equals(info.taskAffinity, FireyerCaseConsts.PACKAGE_NAME + ":fireyerdoc"));
        } else if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.main.VoicerActivity")) {
//            TstRunner.print(tag + "activity voicer theme: " + info.theme, info.theme == R.style.AppTheme);
        } else {
            TstRunner.print(tag + "activity unknown: " + info.name, false);
        }
    }

    public static void doTestServiceInfo(String tag, ServiceInfo info, boolean checkMetaData) {
        if (info == null) return;
        if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.main.MainService")) {
            if (checkMetaData) {
                TstRunner.print(tag + "service main metaData", doCheckMainMetaData(info.metaData));
            }
            TstRunner.print(tag + "service main exported", info.exported);
        } else if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.sub.SubService")) {
            if (checkMetaData) {
                TstRunner.print(tag + "service sub metaData", doCheckSubMetaData(info.metaData));
            }
            TstRunner.print(tag + "service sub exported", !info.exported);
            TstRunner.print(tag + "service sub processName", TextUtils.equals(info.processName, FireyerCaseConsts.PACKAGE_NAME+":Sub"));
        } else {
            TstRunner.print(tag + "service unknown: " + info.name, false);
        }
    }

    public static void doTestProviderInfo(String tag, ProviderInfo info, boolean checkMetaData) {
        if (info == null) return;
        if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.main.MainProvider")) {
            if (checkMetaData) {
                TstRunner.print(tag + "provider main metaData", doCheckMainMetaData(info.metaData));
            }
            TstRunner.print(tag + "provider main exported", info.exported);
            TstRunner.print(tag + "provider main authorities", TextUtils.equals(info.authority, FireyerCaseConsts.PACKAGE_NAME + ".MainProvider"));
        } else if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.sub.SubProvider")) {
            if (checkMetaData) {
                TstRunner.print(tag + "provider sub metaData size", 1 == info.metaData.size());
                int id = info.metaData.getInt("android.support.FILE_PROVIDER_PATHS");
                TstRunner.print(tag + "provider sub metaData xml", TextUtils.equals(doXmlHash(id), "efc1ac17f4b6f78ce88baefb57241ea0"));
            }
            TstRunner.print(tag + "provider sub exported", !info.exported);
            TstRunner.print(tag + "provider sub authorities", TextUtils.equals(info.authority, FireyerCaseConsts.PACKAGE_NAME + ".SubProvider"));
            TstRunner.print(tag + "provider sub grantUriPermissions", info.grantUriPermissions);
            TstRunner.print(tag + "provider sub processName: ", TextUtils.equals(info.processName, FireyerCaseConsts.PACKAGE_NAME+":Sub"));
        } else if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.sub.SubPermissionProvider")) {
            TstRunner.print(tag + "provider sub Permission exported", !info.exported);
            TstRunner.print(tag + "provider sub Permission authorities", TextUtils.equals(info.authority, FireyerCaseConsts.PACKAGE_NAME + ".SubPermissionProvider"));
            TstRunner.print(tag + "provider sub Permission readPermission", TextUtils.equals(info.readPermission, "com.ifma.cmpt.demo.readPermission"));
            TstRunner.print(tag + "provider sub Permission writePermission", TextUtils.equals(info.writePermission, "com.ifma.cmpt.demo.writePermission"));
        } else if (TextUtils.equals(info.name, "androidx.startup.InitializationProvider")) {
            TstRunner.print(tag + "provider startup exported", !info.exported);
            TstRunner.print(tag + "provider startup authorities", TextUtils.equals(info.authority, FireyerCaseConsts.PACKAGE_NAME + ".startup"));
        } else if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.sub.SingleProvider")) {
            TstRunner.print(tag + "provider single exported", !info.exported);
            TstRunner.print(tag + "provider single processName: ", TextUtils.equals(info.processName, FireyerCaseConsts.PACKAGE_NAME+":Single"));
            TstRunner.print(tag + "provider single authorities", TextUtils.equals(info.authority, FireyerCaseConsts.PACKAGE_NAME + ".SingleProvider"));
        } else {
            TstRunner.print(tag + "provider unknown: " + info.name, false);
        }
    }

    public static void doTestReceiverInfo(String tag, ActivityInfo info, boolean checkMetaData) {
        if (info == null) return;
        if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.main.MainReceiver")) {
            if (checkMetaData) {
                TstRunner.print(tag + "receiver main metaData", doCheckMainMetaData(info.metaData));
            }
            TstRunner.print(tag + "receiver main exported", !info.exported);
        } else if (TextUtils.equals(info.name, "com.ifma.cmpt.demo.sub.SubReceiver")) {
            TstRunner.print(tag + "receiver sub exported", info.exported);
            TstRunner.print(tag + "receiver sub processName", TextUtils.equals(info.processName, ".subrcv"));
        } else {
            TstRunner.print(tag + "receiver unknown: " + info.name, false);
        }
    }

    public void doTestComponent(PackageInfo pi, boolean checkMetaData) {
        TstRunner.print("activity count", 5 == pi.activities.length);
        for (ActivityInfo info : pi.activities) {
            doTestActivityInfo("", info, checkMetaData);
        }

        TstRunner.print("service count", 2 == pi.services.length);
        for (ServiceInfo info : pi.services) {
            doTestServiceInfo("", info, checkMetaData);
        }

        TstRunner.print("provider count", 5 == pi.providers.length);
        doTestComponentProviders(pi.providers, checkMetaData);

        TstRunner.print("receiver count", 2 == pi.receivers.length);
        for (ActivityInfo info : pi.receivers) {
            doTestReceiverInfo("", info, checkMetaData);
        }
    }

    void doTestComponentProviders(ProviderInfo[] providers, boolean checkMetaData) {
        for (ProviderInfo info : providers) {
            doTestProviderInfo("", info, checkMetaData);
        }
    }

    static String doXmlHash(int id) {
        InputStream is = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            is = TstEnv.getContext().getResources().openRawResource(id);
            byte[] buffer = new byte[4096];

            int len;
            while ((len = is.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            return CipherUtils.bytesToHexString(md.digest());
        } catch (Throwable var3) {
            Logger.e(var3);
        } finally {
            FileUtils.closeQuietly(is);
        }
        return null;
    }

    public static void dumpApk(Context ctx) {
        ApplicationInfo info = ctx.getApplicationInfo();
        File f = new File(OSUtils.getApkSourceFile(info));
        final String[] src = new String[] {
            MD5Utils.md5WithFile(f),
            String.format("%d", f.length()),
            OSUtils.getApkSourceFile(info)
        };
        ClipboadData.set(ClipboadData.TYPE_Source_APK, src);
    }

    static long sApkSize = 0;
    static String sApkMD5 = null, sSourceApk = null;
    public void testApk() {
        TstRunner.print("signature from pms", doCheckSignature(mPackageInfo.signingInfo));

        PackageInfo pi = mPM.getPackageArchiveInfo(mPackageInfo.applicationInfo.sourceDir,
            PackageManager.GET_META_DATA | PackageManager.GET_ACTIVITIES |
            PackageManager.GET_SERVICES|PackageManager.GET_PROVIDERS|
            PackageManager.GET_RECEIVERS|PackageManager.GET_PERMISSIONS|
            PackageManager.GET_SIGNING_CERTIFICATES|PackageManager.GET_INTENT_FILTERS
        );
        doCheckApplication("local ", pi);
        doTestComponent(pi, false);
        TstRunner.print("local signature from apk", doCheckSignature(pi.signingInfo));

        if (TextUtils.isEmpty(sApkMD5)) {
            final List<String> ss = ClipboadData.get(ClipboadData.TYPE_Source_APK);
            if (null != ss && 1 < ss.size()) {
                sApkMD5 = ss.get(0);
                sApkSize = Long.valueOf(ss.get(1));
            }
        }
        final File apkFile = new File(OSUtils.getApkSourceFile(mPackageInfo.applicationInfo));
        if (sApkSize == apkFile.length()) {
            TstRunner.print("local libc apk size", true);
        } else {
            TstRunner.print("local libc apk size: " + sApkSize + " != " + apkFile.length(), false);
        }
        final long size = FireyerManager.getFileSize(apkFile.getAbsolutePath());
        if (sApkSize == size) {
            TstRunner.print("local svc apk size", true);
        } else {
            TstRunner.print("local svc apk size: " + sApkSize + " != " + size, false);
        }
        final String md5Apk = MD5Utils.md5WithFile(apkFile);
        TstRunner.print("local libc apk md5", TextUtils.equals(md5Apk, sApkMD5));
        final String md5ApkSVC = FireyerUtils.md5File(mPackageInfo.applicationInfo.sourceDir, true);
        TstRunner.print("local svc apk md5", TextUtils.equals(md5ApkSVC, sApkMD5));
    }

    public void testBoundApplication() {
        Object ath = ActivityThreadUtils.getActivityThread();
        Object mBoundApplication = ReflectUtils.getFieldValue(ath, "mBoundApplication");
        ApplicationInfo aInfo = (ApplicationInfo) ReflectUtils.getFieldValue(mBoundApplication, "appInfo");
        doCheckApplicationInfo("AT ", aInfo);

        /**
         * LoadedApk 在初始化时的包名是konker的包名，后续再使用时获取原包包名，会导致LoadedApk被重新加载
         */
        ArrayMap<String, Object> pkgs = (ArrayMap<String, Object>)ReflectUtils.getFieldValue(ath, "mPackages");
        if (null != pkgs) {
            TstRunner.print("AT mPackages count: " + pkgs.size(), pkgs.size() == 1);
            for (String pkg : pkgs.keySet()) {
                if (!TextUtils.equals(FireyerCaseConsts.PACKAGE_NAME, pkg)) {
                    TstRunner.print("AT mPackages: " + pkg, false);
                }
            }
        }
        pkgs = (ArrayMap<String, Object>)ReflectUtils.getFieldValue(ath, "mResourcePackages");
        if (null != pkgs) {
            TstRunner.print("AT mResourcePackages count: " + pkgs.size(), pkgs.size() <= 1);
            for (String pkg : pkgs.keySet()) {
                if (!TextUtils.equals(FireyerCaseConsts.PACKAGE_NAME, pkg)) {
                    TstRunner.print("AT mResourcePackages", false);
                }
            }
        }

        String procName = (String)ReflectUtils.getFieldValue(mBoundApplication, "processName");
        TstRunner.print("mBoundApplication processName", TextUtils.equals(FireyerCaseConsts.PACKAGE_NAME, procName));

        ComponentName cn = (ComponentName)ReflectUtils.getFieldValue(mBoundApplication, "instrumentationName");
        if (null != cn) {
            TstRunner.print("mBoundApplication instrumentationName", TextUtils.equals(FireyerCaseConsts.PACKAGE_NAME, cn.getPackageName()));
        }

        List<ProviderInfo> providers = (List<ProviderInfo>)ReflectUtils.getFieldValue(mBoundApplication, "providers");
        if (null != providers && 0 < providers.size()) {
            ProviderInfo[] pp = new ProviderInfo[providers.size()];
            providers.toArray(pp);
            doTestComponentProviders(pp, false);
        }
    }

    public void testRawResource() {
        try {
            ApplicationInfo pi = getContext().getPackageManager().getApplicationInfo(FireyerCaseConsts.PACKAGE_NAME, PackageManager.GET_META_DATA);
            int id = pi.metaData.getInt("test_res_raw", 0);
            TstRunner.print("res raw id", R.raw.testraw == id);
            TstRunner.print("res raw content", TextUtils.equals(getRawRes(id), "got-this"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private String getRawRes(int id) {
        try (InputStream inputStream = getContext().getResources().openRawResource(id)) {
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            return new String(buffer, 0, len);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
