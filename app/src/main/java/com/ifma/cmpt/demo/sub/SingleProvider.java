package com.ifma.cmpt.demo.sub;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.ifma.cmpt.demo.fireyer.R;
import com.ifma.cmpt.testin.env.TstLogger;


public class SingleProvider extends ContentProvider {
    private static final String TAG = SingleProvider.class.getSimpleName();

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
        return getContext().getResources().getString(R.string.test_single_string);
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
        return null;
    }
}
