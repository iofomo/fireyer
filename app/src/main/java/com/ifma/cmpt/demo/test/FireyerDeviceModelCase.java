package com.ifma.cmpt.demo.test;

import android.os.Build;

import com.ifma.cmpt.fireyer.FireyerManager;
import com.ifma.cmpt.fireyer.FireyerUtils;
import com.ifma.cmpt.testin.module.TstCaseBase;
import com.ifma.cmpt.testin.module.TstRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 设备机型、版本号
 */
public class FireyerDeviceModelCase  extends TstCaseBase {

    /**
     * Build.BOARD  k6833v1_64_k510
     * Build.ID TP1A.220624.014
     * Build.DISPLAY TP1A.220624.014 release-keys
     * Build.PRODUCT PD2318M1
     * Build.DEVICE PD2318
     * Build.MODEL  V2318A
     */

    public void testGetDeviceInfo() {
        String fakeFingerPrint = "fake_fingerprint";
        String realFP = "";
        TstRunner.print("Build.FINGERPRINT:" + Build.FINGERPRINT, fakeFingerPrint.equals(Build.FINGERPRINT));
        //popen("getprop","r")
        realFP = FireyerManager.getPropByPopen("ro.build.fingerprint");
        TstRunner.print("getPropByPopen:" + realFP,fakeFingerPrint.equals(realFP));
        //__system_property_get
        realFP = FireyerManager.getPropBySPG("ro.build.fingerprint");
        TstRunner.print("getPropBySPG:" + realFP,fakeFingerPrint.equals(realFP));
        //__system_property_read_callback
        realFP = FireyerManager.getPropBySPRC("ro.build.fingerprint");
        TstRunner.print("getPropBySPRC:" + realFP,fakeFingerPrint.equals(realFP));
        //W/System.err: java.io.IOException: Cannot run program "getprop": error=-1409064192, Exec failed
        getFromRuntime();
    }

    private static void getFromRuntime(){
        Process process = null;
        BufferedReader reader = null;
        try {
            process =  Runtime.getRuntime().exec("getprop ro.build.fingerprint");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String tmp;
            while ((tmp = reader.readLine()) != null){
                builder.append(tmp);
                builder.append("\n");
            }
           TstRunner.print("runtime fingerprint:"+builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            FireyerUtils.closeQuietly(reader);
            if (process != null){
                process.destroy();
            }
        }
    }
}
