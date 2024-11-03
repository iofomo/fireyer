package com.ifma.cmpt.demo.main;

import android.app.Service;

import com.ifma.cmpt.testin.env.TstHandler;
import com.ifma.cmpt.testin.env.TstLogger;
import com.ifma.cmpt.utils.Logger;
import com.ifma.cmpt.utils.MessengerUtils;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

public class MainService extends Service implements MessengerUtils.IServiceCallback {
    private static final String TAG = MainService.class.getSimpleName();

    private MessengerUtils.ServiceMessenger mMessenger;
    public void onCreate() {
        super.onCreate();
        mMessenger = new MessengerUtils.ServiceMessenger(this, TstHandler.getLooper());
    }

    @Override
    public Message handleMessage(Message msg) {
        if (Logger.D) Logger.d(TAG, "handleMessage: " + msg.toString());

        final Bundle inData = msg.getData();
        final Bundle replyData = new Bundle();
        do {
            if (msg.what != inData.getInt("msg")) break;
            if (1 != inData.size()) break;
            replyData.putInt("call", msg.what);
        } while (false);

        Message outMessage = Message.obtain(null, msg.what);
        outMessage.setData(replyData);
        return outMessage;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TstLogger.d(TAG, intent.toString());
        return START_NOT_STICKY;
    }
}
