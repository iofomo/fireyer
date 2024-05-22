package com.ifma.cmpt.demo.module;

import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Message;

import com.ifma.cmpt.demo.test.FireyerPackageCase;
import com.ifma.cmpt.demo.test.FireyerRuntimeCase;
import com.ifma.cmpt.fireyer.FireyerManager;
import com.ifma.cmpt.utils.ActivityThreadUtils;
import com.ifma.cmpt.utils.Logger;
import com.ifma.cmpt.utils.ReflectUtils;

import java.lang.reflect.Method;
import java.util.List;

public class LauncherMonitor implements Handler.Callback {
    private int LAUNCH_ACTIVITY = 0;
    private int RELAUNCH_ACTIVITY = 0;
    private int CREATE_SERVICE = 0;
    private int INSTALL_PROVIDER = 0;
    private int EXECUTE_TRANSACTION = 0;

    private static LauncherMonitor sSelf = null;
    public static void init() {
        if (sSelf == null) {
            sSelf = new LauncherMonitor();
        }
        sSelf.startMonitor();
    }

    public void startMonitor() {
        Object objH = ActivityThreadUtils.getAtH();
        try {
            FireyerRuntimeCase.sAT_H_Callback = FireyerManager.checkHCallback();

            Class<?> cls = objH.getClass();
            Integer val = (Integer)ReflectUtils.getStaticFieldValue(cls, "LAUNCH_ACTIVITY");
            if (null != val) {
                this.LAUNCH_ACTIVITY = val;
            }

            val = (Integer)ReflectUtils.getStaticFieldValue(cls, "RELAUNCH_ACTIVITY");
            if (null != val) {
                this.RELAUNCH_ACTIVITY = val;
            }

            val = (Integer)ReflectUtils.getStaticFieldValue(cls, "CREATE_SERVICE");
            if (null != val) {
                this.CREATE_SERVICE = val;
            }

            val = (Integer)ReflectUtils.getStaticFieldValue(cls, "INSTALL_PROVIDER");
            if (null != val) {
                this.INSTALL_PROVIDER = val;
            }

            val = (Integer)ReflectUtils.getStaticFieldValue(cls, "EXECUTE_TRANSACTION");
            if (null != val) {
                this.EXECUTE_TRANSACTION = val;
            }
            ReflectUtils.setFieldValue(objH, "mCallback", this);
        } catch (Throwable var7) {
            Logger.e(var7);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        try {
            if (LAUNCH_ACTIVITY == msg.what || RELAUNCH_ACTIVITY == msg.what) {// ActivityThread$ActivityClientRecord
                ActivityInfo ai = (ActivityInfo) ReflectUtils.getFieldValue(msg.obj, "activityInfo");
                FireyerPackageCase.doTestActivityInfo("hcb ", ai, false);
            } else if (CREATE_SERVICE == msg.what) {// ActivityThread$CreateServiceData
                ServiceInfo info = (ServiceInfo) ReflectUtils.getFieldValue(msg.obj, "info");
                FireyerPackageCase.doTestServiceInfo("hcb ", info, false);
            } else if (INSTALL_PROVIDER == msg.what) {
                FireyerPackageCase.doTestProviderInfo("hcb ", (ProviderInfo)msg.obj, false);
            } else if (EXECUTE_TRANSACTION == msg.what) {// for 9.0
                Method m = ReflectUtils.getDeclaredMethod(msg.obj, "getCallbacks");
                List<Object> cbs = (List<Object>) m.invoke(msg.obj);
                if (null != cbs && 0 < cbs.size()) {
                    Class<?> cls = Class.forName("android.app.servertransaction.LaunchActivityItem");
                    for (Object cb : cbs) {
                        if (cls.isInstance(cb)) {
                            // android/app/servertransaction/LaunchActivityItem.mInfo (ActivityInfo)
                            ActivityInfo ai =  (ActivityInfo)ReflectUtils.getFieldValue(cb, "mInfo");
                            FireyerPackageCase.doTestActivityInfo("hcb ", ai, false);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Logger.e(e);
        }
        return false;
    }
}
