package com.ifma.cmpt.demo.module;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClipboadData {
    public static final int TYPE_InstantiateClassLoader         = 1;
    public static final int TYPE_InstantiateApplication         = 2;
    public static final int TYPE_InstantiateActivity            = 3;
    public static final int TYPE_Application_attachBaseContext  = 4;
    public static final int TYPE_Application_onCreate           = 5;
    public static final int TYPE_Provider_onCreate              = 6;
    public static final int TYPE_Activity_onCreate              = 7;
    public static final int TYPE_Thread_Jvm                     = 8;
    public static final int TYPE_Thread_Native                  = 9;
    public static final int TYPE_InstantiateProvider            = 10;
    public static final int TYPE_Source_APK                     = 11;
    public static final int TYPE_Package_Info                   = 12;
    public static final int TYPE_Usb_Info                       = 13;

    private static final String PRE = "tst:";
    private static final Map<Integer, List<String>> sCache = new HashMap<>();

    public static void set(int type, String[] ss) {
        if (null == ss) return;
        List<String> vv = new ArrayList<>();
        for (String s : ss) vv.add(s);
        sCache.put(type, vv);
    }

    public static void set(int type, List<String> v) {
        sCache.put(type, v);
    }

    public static List<String> get(int type) {
        return sCache.get(type);
    }

    public static void saveToClipboard(Context ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(PRE);

        List<String> ss;
        for (Map.Entry<Integer, List<String>> e : sCache.entrySet()) {
            ss = e.getValue();
            Integer key = e.getKey();
            for (String s : ss) {
                sb.append(key).append('=').append(s).append('\n');
            }
        }
        writeToClipboard(ctx, sb.toString());
    }

    public static int loadFromClipboard(Context ctx) {
        sCache.clear();
        String s = readFromClipboard(ctx);
        if (TextUtils.isEmpty(s) || !s.startsWith(PRE)) return 0;

        s = s.substring(PRE.length());
        String[] lines = s.split("\n");

        int pos;
        Integer lastKey = 0;
        List<String> ss = new ArrayList<>();
        for (String item : lines) {
            pos = item.indexOf('=');
            if (pos <= 0) continue;
            Integer key = Integer.valueOf(item.substring(0, pos));
            if (key != lastKey) {
                if (0 < lastKey && 0 < ss.size()) {
                    sCache.put(lastKey, ss);
                    ss = new ArrayList<>();
                }
                lastKey = key;
            }
            ss.add(item.substring(pos+1));
        }
        if (0 < lastKey && 0 < ss.size()) {
            sCache.put(lastKey, ss);
        }
        return sCache.size();
    }

    private static void writeToClipboard(Context ctx, String text) {
        ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", text);
        clipboard.setPrimaryClip(clip);
    }

    private static String readFromClipboard(Context ctx) {
        ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            ClipData.Item item = clip.getItemAt(0);
            return item.getText().toString();
        }
        return null;
    }
}
