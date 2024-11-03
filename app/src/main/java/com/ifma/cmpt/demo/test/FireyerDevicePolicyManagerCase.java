package com.ifma.cmpt.demo.test;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import com.ifma.cmpt.testin.module.TstCaseBase;
import com.ifma.cmpt.testin.module.TstRunner;

import java.util.List;

/**
 * 设备管理器
 */
public class FireyerDevicePolicyManagerCase extends TstCaseBase {

    private static final String TARGET_COMPONENTNAME_PKG = "com.uusafe.frame.nut";
    private static final  String TARGET_COMPONENTNAME_CLS = "com.uusafe.root.policy.DeviceOwnerReceiver";

    public void testDeviceAdmin(){
        Context context = getContext();
        TstRunner.print("DeviceAdmin not found target" , isDeviceAdminFound(context));
        TstRunner.print("deviceAdminEnabled false" , !isDeviceAdminEnabled(context, new ComponentName(TARGET_COMPONENTNAME_PKG, TARGET_COMPONENTNAME_CLS)));
    }

    public void testProfile(){
        Context context = getContext();
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        TstRunner.print("isProfileOwnerApp check",!dpm.isProfileOwnerApp(TARGET_COMPONENTNAME_PKG));
    }

    /**
     * 检查是否有任何设备管理服务被启用
     *
     * @param context 上下文
     * @return 设备管理服务是否被启用
     */
    public static boolean isDeviceAdminFound(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        List<ComponentName> activeAdmins = dpm.getActiveAdmins();
        if (activeAdmins == null) return true;
        for (ComponentName cn:activeAdmins){
            if (TARGET_COMPONENTNAME_PKG.equals(cn.getPackageName()) &&
                    TARGET_COMPONENTNAME_CLS.equals(cn.getClassName())){
                return false;
            }
        }
        return true;
    }

    /**
     * 检查特定的设备管理服务是否启用
     *
     * @param context 上下文
     * @param adminComponent 设备管理服务的组件名称
     * @return 该设备管理服务是否启用
     */
    public static boolean isDeviceAdminEnabled(Context context, ComponentName adminComponent) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isAdminActive(adminComponent);
    }
}
