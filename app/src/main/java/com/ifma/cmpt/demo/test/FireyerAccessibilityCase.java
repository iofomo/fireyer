package com.ifma.cmpt.demo.test;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import com.ifma.cmpt.testin.module.TstCaseBase;
import com.ifma.cmpt.testin.module.TstRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * 辅助功能
 */
public class FireyerAccessibilityCase  extends TstCaseBase {

//    private static final String TARGET_ACCESSIBILITY_SERVICE_ID = "com.mac.accessbility_demo/com.mac.accessibility_demo.AccessibilitySampleService";
    private static final HashSet<String> sHidden_Service_Pkgs = new HashSet<>(Arrays.asList("android.app.se.apop","com.isyner.client"));

    public void testServicesList(){
        Context context = getContext();
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        String svcList = getEnabledAccessibilityServices(context);
        boolean filterFailed = false;
        if (!TextUtils.isEmpty(svcList)){
            for (String pkg:sHidden_Service_Pkgs){
                if (svcList.contains(pkg + "/") || svcList.contains(":" + pkg + "/")){
                    filterFailed = true;
                    break;
                }
            }
        }
        TstRunner.print("svc list:" + svcList,!filterFailed);
        boolean isEnabled = am.isEnabled();
        boolean isTouchExplorationEnabled = am.isTouchExplorationEnabled();
        TstRunner.print("check isEnabled:"+isEnabled+", isTouchExplorationEnabled:"+isTouchExplorationEnabled,((isEnabled || isTouchExplorationEnabled) && !TextUtils.isEmpty(svcList))
                || (!isEnabled && !isTouchExplorationEnabled && TextUtils.isEmpty(svcList)));

    }

    public void testSpecEnable(){
        Context context = getContext();
        boolean isSpecEnable = isSpecificAccessibilityServiceEnabled(context);
        TstRunner.print("isSpecEnable check",!isSpecEnable);
    }



    /**
     * 获取所有已启用的辅助服务
     *
     * @param context 上下文
     * @return 已启用的辅助服务列表字符串
     */
    public static String getEnabledAccessibilityServices(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
    }

    public static boolean isSpecificAccessibilityServiceEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null || !am.isEnabled()) {
            return false;
        }
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo service : enabledServices) {
            String pkg = service.getId().split("/")[0];
            if (sHidden_Service_Pkgs.contains(pkg)){
                return true;
            }
        }
        return false;
    }
}
