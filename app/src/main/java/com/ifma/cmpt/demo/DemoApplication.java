package com.ifma.cmpt.demo;

import android.app.Application;
import android.content.Context;

import com.ifma.cmpt.demo.main.MainReceiver;
import com.ifma.cmpt.demo.module.LauncherMonitor;
import com.ifma.cmpt.demo.test.FireyerRuntimeCase;
import com.ifma.cmpt.demo.test.FireyerStackCase;
import com.ifma.cmpt.fireyer.FireyerManager;
import com.ifma.cmpt.testin.env.TstEnv;
import com.ifma.cmpt.utils.ReflectUtils;

public class DemoApplication extends Application {
    private final static String TAG = "DemoApplication";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        FireyerRuntimeCase.sDemoApplicationABC = true;
        FireyerRuntimeCase.sHiddenApi = FireyerManager.checkHiddenAPI();
        FireyerStackCase.dumpStackForApplication_attachBaseContext();
        TstEnv.init(this);
        MainReceiver.register(base);
//            dumpClassLoader(getClassLoader());
        LauncherMonitor.init();

        clearServiceManager();
//        TstClassPrinter.printStub("android.content.IContentProvider");
//        TstClassPrinter.printStubByCodes("android.content.pm.IPackageManager", 179);
    }

    public static void clearServiceManager() {
        Class<?> cls = ReflectUtils.findClass("android.os.ServiceManager");
        ReflectUtils.setStaticFieldValue(cls, "sServiceManager", null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FireyerRuntimeCase.sDemoApplicationCreate = true;
        FireyerStackCase.dumpStackForApplication_onCreate();
    }

}
