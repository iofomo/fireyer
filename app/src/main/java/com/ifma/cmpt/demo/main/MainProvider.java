package com.ifma.cmpt.demo.main;

import com.ifma.cmpt.cocollider.CoColliderManager;
import com.ifma.cmpt.demo.module.ClassDumper;
import com.ifma.cmpt.demo.module.Consts;
import com.ifma.cmpt.demo.module.PackageDumper;
import com.ifma.cmpt.demo.test.FireyerStackCase;
import com.ifma.cmpt.testin.env.TstLogger;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;


public class MainProvider extends ContentProvider {
    private static final String TAG = MainProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        FireyerStackCase.dumpStackForProvider_onCreate();
        TstLogger.e(TAG, "onCreate");
        getContext().getExternalCacheDir();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        TstLogger.e(TAG, "query");
        return null;
    }

    @Override
    public String getType(Uri uri) {
        TstLogger.e(TAG, "getType");
        return "MainProvider";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        TstLogger.e(TAG, "insert");
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        TstLogger.e(TAG, "delete");
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        TstLogger.e(TAG, "update");
        return 0;
    }

    public Bundle call(String authority, String method, String arg, Bundle extras) {
        Bundle replyData = new Bundle();
        try {
            switch (method) {
                case "dump":
                    handleDumpEvent(arg, extras, replyData);
                    break;
                case "method":// for test case
                    handleTestCaseEvent(authority, arg, extras, replyData);
                    break;
                case "coco":
                    CoColliderManager.handleCallEvent(getContext(), extras, replyData);
                    break;
                default: break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            replyData.putInt(Consts.KEY_CODE, -9);
            replyData.putString(Consts.KEY_MSG, e.getMessage());
        }
        return replyData;
    }

    private void handleDumpEvent(String type, Bundle inData, Bundle replyData) throws Throwable {
        switch (type) {
            case "class":
                ClassDumper.handleCallEvent(getContext(), inData, replyData);
                break;
            case "pkg":
                PackageDumper.handleCallEvent(getContext(), inData, replyData);
                break;
            default: break;
        }
    }

    private void handleTestCaseEvent(String authority, String arg, Bundle extras, Bundle replyData) {
        boolean succ = false;
        do {
            if (!TextUtils.equals(authority, "com.ifma.cmpt.demo.fireyer.MainProvider")) break;
            if (!TextUtils.equals(arg, "arg")) break;
            if (1 != extras.size()) break;
            if (100 != extras.getInt("call")) break;
            succ = true;
        } while (false);
        replyData.putBoolean("call", succ);
    }
}
