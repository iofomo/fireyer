package com.ifma.cmpt.demo.sub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ifma.cmpt.testin.env.TstConsts;
import com.ifma.cmpt.testin.env.TstLogger;


public class SubReceiver extends BroadcastReceiver {
    private static final String TAG = SubReceiver.class.getSimpleName();

    public static void register(Context ctx) {
        ctx.registerReceiver(new SubReceiver(), new IntentFilter(TstConsts.getActionReceiver()));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        TstLogger.bundle(TAG, intent.getExtras());
    }
}
