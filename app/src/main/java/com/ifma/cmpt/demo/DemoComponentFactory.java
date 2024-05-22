package com.ifma.cmpt.demo;

import android.app.Activity;
import android.app.AppComponentFactory;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

import com.ifma.cmpt.demo.test.FireyerPackageCase;
import com.ifma.cmpt.demo.test.FireyerRuntimeCase;
import com.ifma.cmpt.demo.test.FireyerStackCase;
import com.ifma.cmpt.testin.module.TstRunner;

public class DemoComponentFactory extends AppComponentFactory {

    public ClassLoader instantiateClassLoader(ClassLoader cl, ApplicationInfo aInfo) {
        FireyerRuntimeCase.sAppComponentFactoryInitClass = true;
        FireyerPackageCase.doCheckApplicationInfo("ACF ", aInfo);
        FireyerPackageCase.dumpPackageInfo(aInfo);
        FireyerStackCase.dumpStackForInstantiateClassLoader();
        return cl;
    }

    public Application instantiateApplication(ClassLoader cl, String className)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        FireyerRuntimeCase.sAppComponentFactoryInitApp = true;
        FireyerStackCase.dumpStackForInstantiateApplication();
        TstRunner.print("instantiateApplication", "com.ifma.cmpt.demo.FakeApplication".equals(className));
        return (Application) cl.loadClass("com.ifma.cmpt.demo.DemoApplication").newInstance();
    }

    public Activity instantiateActivity(ClassLoader cl,  String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        FireyerRuntimeCase.sAppComponentFactoryInitActivity = true;
        FireyerStackCase.dumpStackForInstantiateActivity();
        TstRunner.print("instantiateActivity",
            TextUtils.equals("com.ifma.cmpt.demo.main.MainActivity", className) ||
            TextUtils.equals("com.ifma.cmpt.demo.main.ConsoleActivity", className) ||
            TextUtils.equals("com.ifma.cmpt.demo.sub.SubActivity", className)
        );
        return (Activity) cl.loadClass(className).newInstance();
    }

    public BroadcastReceiver instantiateReceiver(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TstRunner.print("instantiateReceiver",
            TextUtils.equals("com.ifma.cmpt.demo.main.MainReceiver", className) ||
            TextUtils.equals("com.ifma.cmpt.demo.sub.SubReceiver", className)
        );
        return (BroadcastReceiver) cl.loadClass(className).newInstance();
    }

    public Service instantiateService(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TstRunner.print("instantiateService",
            TextUtils.equals("com.ifma.cmpt.demo.main.MainService", className) ||
            TextUtils.equals("com.ifma.cmpt.demo.sub.SubService", className)
        );
        return (Service) cl.loadClass(className).newInstance();
    }

    public ContentProvider instantiateProvider(ClassLoader cl, String className)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        FireyerRuntimeCase.sAppComponentFactoryInitProvider = true;
        TstRunner.print("instantiateProvider",
            TextUtils.equals("com.ifma.cmpt.demo.main.MainProvider", className) ||
            TextUtils.equals("com.ifma.cmpt.demo.sub.SubProvider", className)
        );
        FireyerStackCase.dumpStackForInstantiateProvider();
        return (ContentProvider) cl.loadClass(className).newInstance();
    }
}
