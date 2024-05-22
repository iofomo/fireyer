package com.ifma.cmpt.demo.module;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class PackageDumper {
    private static final String TAG = "PackageDumper";

    public static void handleCallEvent(Context ctx, Bundle inData, Bundle replayData) throws Throwable {
        final String pkg = inData.getString(Consts.KEY_IN_ARG1);
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(pkg, 0);
        replayData.putString("verName", pi.versionName);
        replayData.putLong("verCode", pi.getLongVersionCode());
        replayData.putInt("minSdk", pi.applicationInfo.minSdkVersion);
        replayData.putInt("tarSdk", pi.applicationInfo.targetSdkVersion);
        replayData.putString("appComp", pi.applicationInfo.appComponentFactory);
        CharSequence s = pm.getApplicationLabel(pi.applicationInfo);
        replayData.putString("label", s.toString());
    }

}
