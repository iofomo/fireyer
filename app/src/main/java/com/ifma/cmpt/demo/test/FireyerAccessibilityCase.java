package com.ifma.cmpt.demo.test;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.ifma.cmpt.testin.module.TstCaseBase;
import com.ifma.cmpt.testin.module.TstRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class FireyerAccessibilityCase  extends TstCaseBase {

//    private static final String TARGET_ACCESSIBILITY_SERVICE_ID = "com.mac.accessbility_demo/com.mac.accessibility_demo.AccessibilitySampleService";
    private static final HashSet<String> sHidden_Service_Pkgs = new HashSet<>(Arrays.asList("android.app.se.apop","com.isyner.client"));

    @Override
    protected void tearDown() throws Exception {
        Context context = getContext();
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        String svcList = getEnabledAccessibilityServices(context);
        boolean filterFailed = false;
        for (String pkg:sHidden_Service_Pkgs){
            if (svcList.contains(pkg + "/") || svcList.contains(":" + pkg + "/")){
                filterFailed = true;
                break;
            }
        }
        TstRunner.print("svc list:" + svcList,!filterFailed);
        boolean isEnabled = am.isEnabled();
        boolean isTouchExplorationEnabled = am.isTouchExplorationEnabled();
        TstRunner.print("check isEnabled:"+isEnabled+", isTouchExplorationEnabled:"+isTouchExplorationEnabled,((isEnabled || isTouchExplorationEnabled) && !TextUtils.isEmpty(svcList))
                                            || (!isEnabled && !isTouchExplorationEnabled && TextUtils.isEmpty(svcList)));
        boolean isSpecEnable = isSpecificAccessibilityServiceEnabled(context);
        if (isSpecEnable){
            TstRunner.print("isSpecEnable check fail",false);
        }
        super.tearDown();
    }

    private boolean isHiddenService(String id){
        if (TextUtils.isEmpty(id) || !id.contains("/")){
            return false;
        }
        String targetPkg = id.split("/")[0];
        return sHidden_Service_Pkgs.contains(targetPkg);
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


    /**
     * 以仅示例的形式检查特定辅助服务是否启用。
     * Note: 实际中需要其他途径，如Settings.Secure检查。
     */
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
