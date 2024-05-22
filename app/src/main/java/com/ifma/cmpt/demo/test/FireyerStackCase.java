/**
 * @brief: test case
 *      only the method which is public and start with "test" will be run, such as:
 *      public void testXXX() {
 *          // TODO something ...
 *      }
 * */
package com.ifma.cmpt.demo.test;

import android.os.Build;
import android.text.TextUtils;

import com.ifma.cmpt.demo.module.ClipboadData;
import com.ifma.cmpt.testin.module.TstCaseBase;
import com.ifma.cmpt.testin.module.TstRunner;
import com.ifma.cmpt.utils.OSUtils;

import java.util.ArrayList;
import java.util.List;

public class FireyerStackCase extends TstCaseBase {
    private static final String TAG = "FireyerStackCase";

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private static void dumpThreadStack(int type, List<String> stacks) {
        try {
            StackTraceElement[] ee = new Exception("Stack trace").getStackTrace();
            for (StackTraceElement e : ee) {
                stacks.add(e.getClassName() + "." + e.getMethodName());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        ClipboadData.set(type, stacks);
    }

    private static List<String> sActivity_onCreate = new ArrayList<>();
    public static void dumpStackForActivity_onCreate() {
        dumpThreadStack(ClipboadData.TYPE_Activity_onCreate, sActivity_onCreate);
    }

    private static List<String> sInstantiateClassLoader = new ArrayList<>();
    public static void dumpStackForInstantiateClassLoader() {
        dumpThreadStack(ClipboadData.TYPE_InstantiateClassLoader, sInstantiateClassLoader);
    }

    private static List<String> sInstantiateApplication = new ArrayList<>();
    public static void dumpStackForInstantiateApplication() {
        dumpThreadStack(ClipboadData.TYPE_InstantiateApplication, sInstantiateApplication);
    }

    private static List<String> sInstantiateActivity = new ArrayList<>();
    public static void dumpStackForInstantiateActivity() {
        dumpThreadStack(ClipboadData.TYPE_InstantiateActivity, sInstantiateActivity);
    }

    private static List<String> sApplication_attachBaseContext = new ArrayList<>();
    public static void dumpStackForApplication_attachBaseContext() {
        dumpThreadStack(ClipboadData.TYPE_Application_attachBaseContext, sApplication_attachBaseContext);
    }

    private static List<String> sApplication_onCreate = new ArrayList<>();
    public static void dumpStackForApplication_onCreate() {
        dumpThreadStack(ClipboadData.TYPE_Application_onCreate, sApplication_onCreate);
    }

    private static List<String> sInstantiateProvider  = new ArrayList<>();
    public static void dumpStackForInstantiateProvider () {
        dumpThreadStack(ClipboadData.TYPE_InstantiateProvider , sInstantiateProvider);
    }

    private static List<String> sProvider_onCreate  = new ArrayList<>();
    public static void dumpStackForProvider_onCreate () {
        dumpThreadStack(ClipboadData.TYPE_Provider_onCreate , sProvider_onCreate);
    }

    private static void compareStacks(String tag, List<String> currStacks, List<String> cacheStacks) {
        boolean succ = true;
        int i = 0;
        while (i < currStacks.size() && i < cacheStacks.size()) {
            if (!TextUtils.equals(currStacks.get(i), cacheStacks.get(i))) {
                succ = false;
                TstRunner.print(tag + " unknown stack: " + currStacks.get(i), false);
            }
            ++ i;
        }
        if (cacheStacks.size() < currStacks.size()) {
            while (i < currStacks.size()) {
                succ = false;
                TstRunner.print(tag + " unknown stack: " + currStacks.get(i), false);
                ++ i;
            }
        } else if (currStacks.size() < cacheStacks.size()) {
            while (i < cacheStacks.size()) {
                succ = false;
                TstRunner.print(tag + " miss stack: " + cacheStacks.get(i), false);
                ++ i;
            }
        }

        if (succ) {
            TstRunner.print(tag + " stack", true);
        }
    }

    private static void doTestStack(String tag, int type, List<String> currStacks) {
        List<String> cacheStacks = ClipboadData.get(type);
        if (null == cacheStacks) {
            TstRunner.print(tag + " stack: " + currStacks.size() + " == null(origin)", false);
        } else {
            compareStacks(tag, currStacks, cacheStacks);
        }
    }

    public static void testStack() {
        if (OSUtils.ENV_SDK_INT_P_28_9 < Build.VERSION.SDK_INT) {
            doTestStack("InstantiateClassLoader", ClipboadData.TYPE_InstantiateClassLoader, sInstantiateClassLoader);
        }
        doTestStack("InstantiateApplication", ClipboadData.TYPE_InstantiateApplication, sInstantiateApplication);
        doTestStack("InstantiateActivity", ClipboadData.TYPE_InstantiateActivity, sInstantiateActivity);
        doTestStack("InstantiateProvider", ClipboadData.TYPE_InstantiateProvider, sInstantiateProvider);
        doTestStack("attachBaseContext", ClipboadData.TYPE_Application_attachBaseContext, sApplication_attachBaseContext);
        doTestStack("Application_onCreate", ClipboadData.TYPE_Application_onCreate, sApplication_onCreate);
        doTestStack("Provider_onCreate", ClipboadData.TYPE_Provider_onCreate, sProvider_onCreate);
        doTestStack("Activity_onCreate", ClipboadData.TYPE_Activity_onCreate, sActivity_onCreate);
    }
}
