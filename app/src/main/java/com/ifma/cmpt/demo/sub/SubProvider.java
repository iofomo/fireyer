package com.ifma.cmpt.demo.sub;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.ifma.cmpt.testin.env.TstLogger;


public class SubProvider extends ContentProvider {
    private static final String TAG = SubProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        TstLogger.e(TAG, "onCreate");
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
        return "SubProvider";
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
        TstLogger.e(TAG, "call");
        TstLogger.bundle(TAG, extras);
        boolean succ = false;
        do {
            if (!TextUtils.equals(authority, "com.ifma.cmpt.demo.fireyer.SubProvider")) break;
            if (!TextUtils.equals(method, "method")) break;
            if (!TextUtils.equals(arg, "arg")) break;
            if (1 != extras.size()) break;
            if (100 != extras.getInt("call")) break;
            succ = true;
        } while (false);

        Bundle replyData = new Bundle();
        replyData.putBoolean("call", succ);
        return replyData;
    }
}
