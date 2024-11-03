package com.ifma.cmpt.demo.sub;

import android.app.Service;
import android.content.Intent;
import android.media.CreateRecordRequest;
import android.media.CreateRecordResponse;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import com.ifma.cmpt.demo.IBinderTest;
import com.ifma.cmpt.demo.IBinderTestCallback;
import com.ifma.cmpt.demo.test.FireyerCaseConsts;
import com.ifma.cmpt.testin.env.TstHandler;
import com.ifma.cmpt.testin.env.TstLogger;
import com.ifma.cmpt.utils.Logger;
import com.ifma.cmpt.utils.MessengerUtils;

public class SubService extends Service implements MessengerUtils.IServiceCallback {
    private static final String TAG = SubService.class.getSimpleName();

    public static final String ACTION_BINDER = "key-binder";
    public static final String ACTION_MESSAGE = "key-message";

    MessengerUtils.ServiceMessenger mMessenger;
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
        if (TextUtils.equals(ACTION_BINDER, intent.getAction())) {
            FireyerCaseConsts.setMode(intent.getIntExtra(FireyerCaseConsts.KEY_MODE, FireyerCaseConsts.MODE_SOURCE));
            return mBinderTest;
        }
        if (TextUtils.equals(ACTION_MESSAGE, intent.getAction())) {
            return mMessenger.getBinder();
        }
        return null;
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

    private static String getPre()  {
        return FireyerCaseConsts.isSourceMode() ? "" : FireyerCaseConsts.KEY_PRE;
    }

    private final IBinderTest.Stub mBinderTest = new IBinderTest.Stub() {

        @Override
        public int callBinderTest1(String a, IBinder c, int d) throws RemoteException {
            Logger.d(TAG, "callBinderTest1 >>>");
            Logger.d(TAG, "callBinderTest1 a: " + a + " -> " +  getPre() + FireyerCaseConsts.KEY_BINDER_ARG_A);
            Logger.d(TAG, "callBinderTest1 d: " + d);
            Logger.d(TAG, "callBinderTest binder c: " + c);
            if (!TextUtils.equals(a, getPre() + FireyerCaseConsts.KEY_BINDER_ARG_A)) return -1;
            if (d != 100) return -5;
            if (null == c) return -6;
            IBinderTestCallback cb = IBinderTestCallback.Stub.asInterface(c);
            if (2 != cb.handleBinderTestCallback(1)) return -8;

            return 0;
        }

        @Override
        public int callBinderTest2(int a, IBinder b, String c) throws RemoteException {
            Logger.d(TAG, "callBinderTest2 >>>");
            Logger.d(TAG, "callBinderTest2 a: " + a);
            Logger.d(TAG, "callBinderTest2 c: " + c);
            Logger.d(TAG, "callBinderTest binder b: " + b);
            if (!TextUtils.equals(c, getPre() + FireyerCaseConsts.KEY_BINDER_ARG_C)) return -1;
            if (a != 100) return -5;
            if (null == b) return -6;
            IBinderTestCallback cb = IBinderTestCallback.Stub.asInterface(b);
            if (2 != cb.handleBinderTestCallback(1)) return -8;

            return 0;
        }

        public int callBinderTest(String a, String b, IBinder c, int d, String e, IBinder f, String g) throws RemoteException {
            Logger.d(TAG, "callBinderTest >>>");
            Logger.d(TAG, "callBinderTest a: " + a);
            Logger.d(TAG, "callBinderTest b: " + b);
            Logger.d(TAG, "callBinderTest e: " + e);
            Logger.d(TAG, "callBinderTest g: " + g);
            Logger.d(TAG, "callBinderTest binder c: " + c);
            Logger.d(TAG, "callBinderTest binder f: " + f);
            if (!TextUtils.equals(a, getPre() + FireyerCaseConsts.KEY_BINDER_ARG_A)) return -1;
            if (!TextUtils.equals(b, getPre() + FireyerCaseConsts.KEY_BINDER_ARG_B)) return -2;
            if (!TextUtils.equals(e, getPre() + FireyerCaseConsts.KEY_BINDER_ARG_C)) return -3;
            if (!TextUtils.equals(g, getPre() + FireyerCaseConsts.KEY_BINDER_ARG_D)) return -4;
            if (d != 100) return -5;
            if (null == c) return -6;
            if (null == f) return -7;
            IBinderTestCallback cb = IBinderTestCallback.Stub.asInterface(c);
            if (2 != cb.handleBinderTestCallback(1)) return -8;

            cb = IBinderTestCallback.Stub.asInterface(f);
            if (3 != cb.handleBinderTestCallback(2)) return -9;

            return 0;
        }

        @Override
        public CreateRecordResponse createRecord(CreateRecordRequest request) throws RemoteException {
            Logger.d("xxx", "pkg: " + request.clientInfo.attributionSource.packageName);
            return null;
        }

    };
}
