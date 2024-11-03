/**
 * @brief: test case
 *      only the method which is public and start with "test" will be run, such as:
 *      public void testXXX() {
 *          // TODO something ...
 *      }
 * */
package com.ifma.cmpt.demo.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AttributionSourceState;
import android.media.AudioAttributesInternal;
import android.media.AudioClient;
import android.media.AudioConfigBase;
import android.media.CreateRecordRequest;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import com.ifma.cmpt.demo.IBinderTest;
import com.ifma.cmpt.demo.IBinderTestCallback;
import com.ifma.cmpt.demo.main.MainService;
import com.ifma.cmpt.demo.module.ClipboadData;
import com.ifma.cmpt.demo.sub.SubService;
import com.ifma.cmpt.fireyer.FireyerManager;
import com.ifma.cmpt.testin.env.TstHandler;
import com.ifma.cmpt.testin.module.TstCaseBase;
import com.ifma.cmpt.testin.module.TstRunner;
import com.ifma.cmpt.utils.Logger;
import com.ifma.cmpt.utils.MessengerUtils;
import com.ifma.cmpt.utils.OSUtils;
import com.ifma.cmpt.utils.ShellUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FireyerRuntimeCase extends TstCaseBase implements Handler.Callback {
    private static final String TAG = "FireyerRuntimeCase";

    protected void setUp() throws Exception {
        super.setUp();
        firstTestProc();
    }

    protected void tearDown() throws Exception {
        lastTestMaps();
        checkBinderTestResult();
        super.tearDown();
    }

    public void firstTestProc() {
        String pname = OSUtils.getProcessName();
        TstRunner.print("cmdline", TextUtils.equals(pname.trim(), FireyerCaseConsts.PACKAGE_NAME));

        Set<FireyerManager.IdItem> items = FireyerManager.getOtherProcess(getContext());
        boolean pass = null == items;
        if (!pass && items.size() == 1) {
            for (FireyerManager.IdItem item : items) {
                pass = "getprop".equals(item.name);
            }
        }
        TstRunner.print("process id", pass);
        if (null != items) {
            for (FireyerManager.IdItem item : items) {
                TstRunner.print("process id: " + item.id + ", " + item.name);
            }
        }
    }

    public void testTrace() {
        TstRunner.print("trace", 0 == FireyerManager.getTracerPid());
    }

    private static String converThreadName(String name) {
        if (name.startsWith("Binder:")) {
            int pos = name.indexOf("_");
            if (0 < pos) {
                name = "Binder:" + name.substring(pos+1);
            }
        }
        return name;
    }

    private static List<String> itemToString(Set<FireyerManager.IdItem> items) {
        List<String> lines = new ArrayList<>();
        for (FireyerManager.IdItem item : items) {
            // Binder:18542_5 -> Binder:5
            lines.add(converThreadName(item.name));
        }
        return lines;
    }

    static Set<FireyerManager.IdItem> sJvmThreads;
    static Set<FireyerManager.IdItem> sNativeThreads;
    public static void dumpThread() {
        sNativeThreads = FireyerManager.getNativeThreads();
        if (null != sNativeThreads && 0 < sNativeThreads.size()) {
            ClipboadData.set(ClipboadData.TYPE_Thread_Native, itemToString(sNativeThreads));
        }
        sJvmThreads = FireyerManager.getJvmThreads();
        if (null != sJvmThreads && 0 < sJvmThreads.size()) {
            ClipboadData.set(ClipboadData.TYPE_Thread_Jvm, itemToString(sJvmThreads));
        }
    }

    public void doTestThread(String tag, int type) {
        Set<FireyerManager.IdItem> items = ClipboadData.TYPE_Thread_Jvm == type ? sJvmThreads : sNativeThreads;
        if (null != items && 0 < items.size()) {
            List<String> caches = ClipboadData.get(type);
            if (null != caches) {
                TstRunner.print(tag + " thread : " + items.size() + " == " + caches.size(), items.size() == caches.size());
                Set<String> ss = new HashSet<>();
                ss.addAll(caches);
                for (FireyerManager.IdItem item : items) {
                    String name = converThreadName(item.name);
                    if (ss.contains(name)) continue;
                    TstRunner.print(tag + " unknown thread : " + item.name, false);
                }
            } else {
                TstRunner.print(tag + " thread count: " + items.size());
            }
        } else {
            TstRunner.print(tag + " thread count: 0", false);
        }
    }

    public void testThread() {
        doTestThread("JVM", ClipboadData.TYPE_Thread_Jvm);
        doTestThread("Native", ClipboadData.TYPE_Thread_Native);
    }

    public void lastTestMaps() {
        List<String> maps = new ArrayList<>();
        boolean succ = FireyerManager.getOtherMapsLibs(maps);
        TstRunner.print("maps", succ);
        if (null != maps) {
            for (String map : maps) {
                TstRunner.print(map);
            }
        }

        maps.clear();
        succ = FireyerManager.getOtherMapsLibsBySVC(maps);
        TstRunner.print("maps svc", succ);
        if (null != maps) {
            for (String map : maps) {
                TstRunner.print(map);
            }
        }
    }

    public static boolean sHiddenApi = false;
    public void testHiddenAPI() {
        TstRunner.print("hidden api", sHiddenApi);
    }

    public static boolean sFakeApplicationABC = true;
    public static boolean sFakeApplicationCreate = true;
    public static boolean sDemoApplicationABC = false;
    public static boolean sDemoApplicationCreate = false;
    public static boolean sAppComponentFactoryInitClass = false;
    public static boolean sAppComponentFactoryInitApp = false;
    public static boolean sAppComponentFactoryInitActivity = false;
    public static boolean sAppComponentFactoryInitProvider = false;
    public void testAppLaunch() {
        TstRunner.print("FakeApplication attachBaseContext", sFakeApplicationABC);
        TstRunner.print("FakeApplication onCreate", sFakeApplicationCreate);
        TstRunner.print("DemoApplication attachBaseContext", sDemoApplicationABC);
        TstRunner.print("DemoApplication onCreate", sDemoApplicationCreate);
        if (OSUtils.ENV_SDK_INT_Q_29_10 <= Build.VERSION.SDK_INT) {
            TstRunner.print("AppComponentFactory instantiateClassLoader", sAppComponentFactoryInitClass);
        }
        TstRunner.print("AppComponentFactory instantiateApplication", sAppComponentFactoryInitApp);
        TstRunner.print("AppComponentFactory instantiateActivity", sAppComponentFactoryInitActivity);
        TstRunner.print("AppComponentFactory instantiateProvider", sAppComponentFactoryInitProvider);
    }

    public void testLocalFiles() {
        TstRunner.print("local file or dir", null == FireyerManager.getOtherLocalFiles(getContext()));
        TstRunner.print("local file or dir svc", null == FireyerManager.getOtherLocalFilesBySVC(getContext()));
    }

    public void testSingleProvder() {
        Uri uri = Uri.parse("content://com.ifma.cmpt.demo.fireyer.SingleProvider");
        String typ = getContext().getContentResolver().getType(uri);
        TstRunner.print("single pvd: " + typ, "single".equals(typ));
    }

    public void testBinder() throws Throwable {
        TstRunner.print("binder", FireyerManager.checkBinder());
    }

    public void testStack() throws Throwable {
        TstRunner.print("function stack", FireyerManager.checkStack());
    }

    public static boolean sAT_H_Callback = false;
    public void testContext() throws Throwable {
        TstRunner.print("application",
            FireyerManager.checkApplication(getContext(),
                "com.ifma.cmpt.demo.DemoApplication",
                "com.ifma.cmpt.demo.DemoComponentFactory",
                FireyerCaseConsts.PACKAGE_NAME
            )
        );
        TstRunner.print("class loader", FireyerManager.checkClassLoader(getContext()));
        TstRunner.print("activityHCallback", sAT_H_Callback);
    }

    private MessengerUtils.ClientMessenger mMainMessenger;
    private MessengerUtils.ClientMessenger mSubMessenger;
    public void testBindService() {
        if (null == mMainMessenger) {
            mMainMessenger = new MessengerUtils.ClientMessenger(this, TstHandler.getLooper());
        }
        if (mMainMessenger.isActive()) {
            doSendMainMessage();
        } else {
            getContext().bindService(
                    new Intent(getContext(), MainService.class),
                    mMainConnection,
                    Context.BIND_AUTO_CREATE
            );
        }

        if (null == mSubMessenger) {
            mSubMessenger = new MessengerUtils.ClientMessenger(this, TstHandler.getLooper());
        }
        if (mSubMessenger.isActive()) {
            doSendSubMessage();
        } else {
            Intent intent = new Intent(getContext(), SubService.class);
            intent.setAction(SubService.ACTION_MESSAGE);
            getContext().bindService(
                    intent,
                    mSubConnection,
                    Context.BIND_AUTO_CREATE
            );
        }
    }

    public void testStartService() {
        Intent intent = new Intent(getContext(), MainService.class);
        intent.setPackage(getContext().getPackageName());
        intent.setAction(getContext().getPackageName() + ".MainAction");
        getContext().startService(intent);
        TstRunner.print("Intent main packagename", TextUtils.equals(intent.getPackage(), FireyerCaseConsts.PACKAGE_NAME));
        ComponentName cn = intent.getComponent();
        TstRunner.print("Intent main cn packagename", TextUtils.equals(cn.getPackageName(), FireyerCaseConsts.PACKAGE_NAME));
        TstRunner.print("Intent main cn class", TextUtils.equals(MainService.class.getName(), cn.getClassName()));

        intent = new Intent(getContext(), SubService.class);
        intent.setPackage(getContext().getPackageName());
        intent.setAction(getContext().getPackageName() + ".SubAction");
        getContext().startService(intent);
        TstRunner.print("Intent sub packagename", TextUtils.equals(intent.getPackage(), FireyerCaseConsts.PACKAGE_NAME));
        cn = intent.getComponent();
        TstRunner.print("Intent sub cn packagename", TextUtils.equals(cn.getPackageName(), FireyerCaseConsts.PACKAGE_NAME));
        TstRunner.print("Intent sub cn class", TextUtils.equals(SubService.class.getName(), cn.getClassName()));
    }

    private ServiceConnection mMainConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            TstRunner.print("Main ServiceConnected", TextUtils.equals(className.getPackageName(), FireyerCaseConsts.PACKAGE_NAME) &&
                    TextUtils.equals(className.getClassName(), MainService.class.getName())
                    );
            try {
                Messenger messenger = new Messenger(service);
                Message message = new Message();
                Intent i = new Intent();
                i.setComponent(className);
                message.getData().putParcelable("intent", i);
                messenger.send(message);
            } catch (Throwable th) {
                TstRunner.print("ServiceConnected exception", false);
                th.printStackTrace();
            }
            mMainMessenger.setServiceBinder(service);
            doSendMainMessage();
        }

        public void onServiceDisconnected(ComponentName className) {
            mMainMessenger.setServiceBinder(null);
        }
    };

    private ServiceConnection mSubConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            TstRunner.print("Sub ServiceConnected", TextUtils.equals(className.getPackageName(), FireyerCaseConsts.PACKAGE_NAME) &&
                    TextUtils.equals(className.getClassName(), SubService.class.getName())
            );
            mSubMessenger.setServiceBinder(service);
            doSendSubMessage();
        }

        public void onServiceDisconnected(ComponentName className) {
            mSubMessenger.setServiceBinder(null);
        }
    };

    public void testBinderCall() {
        if (mBinderTest != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (null == mBinderTest) return;
                    try {
                        sBinderTestResult1 = mBinderTest.callBinderTest1(
                                FireyerCaseConsts.KEY_BINDER_ARG_A,
                                createBinderTestCallback().asBinder(),
                                100
                        );
                        sBinderTestResult2 = mBinderTest.callBinderTest2(
                                100,
                                createBinderTestCallback().asBinder(),
                                FireyerCaseConsts.KEY_BINDER_ARG_C
                        );
                        sBinderTestResult = mBinderTest.callBinderTest(
                                FireyerCaseConsts.KEY_BINDER_ARG_A,
                                FireyerCaseConsts.KEY_BINDER_ARG_B,
                                createBinderTestCallback().asBinder(),
                                100,
                                FireyerCaseConsts.KEY_BINDER_ARG_C,
                                createBinderTestCallback().asBinder(),
                                FireyerCaseConsts.KEY_BINDER_ARG_D
                        );

                        CreateRecordRequest rr = new CreateRecordRequest();
                        rr.attr = new AudioAttributesInternal();
                        rr.config = new AudioConfigBase();
                        rr.clientInfo = new AudioClient();
                        rr.clientInfo.clientTid = 1;
                        rr.clientInfo.attributionSource = new AttributionSourceState();
                        rr.clientInfo.attributionSource.packageName = FireyerCaseConsts.PACKAGE_NAME;
                        rr.clientInfo.attributionSource.token = new Binder();
                        mBinderTest.createRecord(rr);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Intent intent = new Intent(getContext(), SubService.class);
            intent.setAction(SubService.ACTION_BINDER);
            intent.putExtra(FireyerCaseConsts.KEY_MODE, FireyerCaseConsts.getMode());
            getContext().bindService(
                    intent,
                    mBinderConnection,
                    Context.BIND_AUTO_CREATE
            );
        }
    }

    IBinderTest mBinderTest = null;
    static int sBinderTestResult1 = -100;
    static int sBinderTestResult2 = -100;
    static int sBinderTestResult = -100;
    private ServiceConnection mBinderConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            TstRunner.print("Binder ServiceConnected", TextUtils.equals(className.getPackageName(), FireyerCaseConsts.PACKAGE_NAME) &&
                    TextUtils.equals(className.getClassName(), SubService.class.getName())
            );
            mBinderTest = IBinderTest.Stub.asInterface(service);
            testBinderCall();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBinderTest = null;
        }
    };

    static IBinderTestCallback createBinderTestCallback() {
        return new IBinderTestCallback.Stub() {
            @Override
            public int handleBinderTestCallback(int a) throws RemoteException {
                return a + 1;
            }
        };
    }

    void checkBinderTestResult() {
        if (-100 == sBinderTestResult) {
            TstRunner.print("wait binder test result");
            int cnt = 10;
            do {
                try {
                    Thread.sleep(500);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } while (-100 == sBinderTestResult && 0 < cnt--);
        }
        TstRunner.print("binder test1" + (0 == sBinderTestResult1 ? "" : ": " + sBinderTestResult1), 0 == sBinderTestResult1);
        TstRunner.print("binder test2" + (0 == sBinderTestResult2 ? "" : ": " + sBinderTestResult2), 0 == sBinderTestResult2);
        TstRunner.print("binder test" + (0 == sBinderTestResult ? "" : ": " + sBinderTestResult), 0 == sBinderTestResult);
    }

    private static final int MESSAGE_WHAT_MAIN = 100;
    private static final int MESSAGE_WHAT_SUB = 101;

    private void doSendMainMessage() {
        Bundle data = new Bundle();
        data.putInt("msg", MESSAGE_WHAT_MAIN);
        mMainMessenger.send(MESSAGE_WHAT_MAIN, data);
    }

    private void doSendSubMessage() {
        Bundle data = new Bundle();
        data.putInt("msg", MESSAGE_WHAT_SUB);
        mSubMessenger.send(MESSAGE_WHAT_SUB, data);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (Logger.D) Logger.d(TAG, "handle msg: " + msg.toString());
        if (msg.what == MESSAGE_WHAT_MAIN) {
            TstRunner.print("MainService", MESSAGE_WHAT_MAIN == msg.getData().getInt("call"));
        } else if (msg.what == MESSAGE_WHAT_SUB) {
            TstRunner.print("SubService", MESSAGE_WHAT_SUB == msg.getData().getInt("call"));
        }
        return true;
    }

    public void testProvider() {
        Uri uri = Uri.parse("content://com.ifma.cmpt.demo.fireyer.SubProvider");
        String typ = getContext().getContentResolver().getType(uri);
        TstRunner.print("SubProvider getType", TextUtils.equals("SubProvider", typ));

        Bundle data = new Bundle();
        data.putInt("call", 100);
        Bundle result = getContext().getContentResolver().call(uri, "method", "arg", data);
        TstRunner.print("SubProvider call", null != result && result.getBoolean("call"));

        uri = Uri.parse("content://com.ifma.cmpt.demo.fireyer.MainProvider");
        typ = getContext().getContentResolver().getType(uri);
        TstRunner.print("MainProvider getType", TextUtils.equals("MainProvider", typ));

        result = getContext().getContentResolver().call(uri, "method", "arg", data);
        TstRunner.print("MainProvider call", null != result && result.getBoolean("call"));
    }

    public void testExecve() {
        String fileName = "exe-tst." + System.currentTimeMillis();
        File f = new File(getContext().getDataDir(), fileName);
        try {
            File[] ff = f.getParentFile().listFiles();
            if (null != ff) {
                for (File file : ff) {
                    if (file.getName().startsWith("exe-tst.")) file.delete();
                }
            }

            f.createNewFile();
            TstRunner.print("Execve: create file 1", f.isFile());
            TstRunner.print("Execve: create file 2", new File("/data/data/com.ifma.cmpt.demo.fireyer/" + f.getName()).isFile());

            String result;
            ShellUtils.ShellResult sr = ShellUtils.execute("ls " + f.getParent());
            if (null != sr) {
                result = sr.getOutput();
                TstRunner.print("out: " + result);
                if (sr.hasErrput()) {
                    TstRunner.print("err: " + sr.getErrput());
                }
                TstRunner.print("Execve: ls 1", 0 < result.indexOf(fileName));
            } else {
                TstRunner.print("Execve: ls 1 failed", false);
            }

            sr = ShellUtils.execute("ls " + "/data/data/com.ifma.cmpt.demo.fireyer/");
            if (null != sr) {
                result = sr.getOutput();
                TstRunner.print("out: " + result);
                if (sr.hasErrput()) {
                    TstRunner.print("err: " + sr.getErrput());
                }
                TstRunner.print("Execve: ls 2", 0 < result.indexOf(fileName));
            } else {
                TstRunner.print("Execve: ls 2 failed", false);
            }

            sr = ShellUtils.execute("mv " + f.getAbsolutePath() + " " + f.getAbsolutePath() + "-abc");
            if (null != sr) {
                if (sr.hasErrput()) Logger.e(TAG, sr.getErrput());
                if (sr.hasOutput()) Logger.e(TAG, sr.getOutput());
            } else {
                TstRunner.print("Execve: mv failed", false);
            }

            sr = ShellUtils.execute("ls " + "/data/data/com.ifma.cmpt.demo.fireyer/");
            if (null != sr) {
                result = sr.getOutput();
                TstRunner.print("out: " + result);
                if (sr.hasErrput()) {
                    TstRunner.print("err: " + sr.getErrput());
                }
                TstRunner.print("Execve: ls 3", 0 < result.indexOf(fileName + "-abc"));
            } else {
                TstRunner.print("Execve: ls 3 failed", false);
            }
        } catch (Throwable e) {
            TstRunner.print("Execve: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    public void testUsb() {
        List<String> infos = new ArrayList<>();
        UsbManager mgr = (UsbManager)getContext().getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> map = mgr.getDeviceList();
        TstRunner.print("Usb dev: " + (null == map ? "null": map.size()));
        if (null != map) {
            for (Map.Entry<String, UsbDevice> entry: map.entrySet()) {
                try {
                    String info = "dev: " + entry.getKey() + ": " + entry.getValue().getSerialNumber();
                    TstRunner.print(info);
                    infos.add(info);
                } catch (Throwable e) {
                    TstRunner.print(e.getMessage());
                }
            }
        }
        UsbAccessory[] accs = mgr.getAccessoryList();
        TstRunner.print("Usb acc: " + (null == accs ? "null": accs.length));
        if (null != accs) {
            for (UsbAccessory acc : accs) {
                try {
                    String info = "acc: " + acc.getSerial();
                    TstRunner.print(info);
                    infos.add(info);
                } catch (Throwable e) {
                    TstRunner.print(e.getMessage());
                }
            }
        }
        if (FireyerCaseConsts.isVirtualMode()) {
            List<String> val = ClipboadData.get(ClipboadData.TYPE_Usb_Info);
            TstRunner.print("usb info ", doCompareList(val, infos));
        } else {
            ClipboadData.set(ClipboadData.TYPE_Usb_Info, infos);
        }
    }

    private boolean doCompareList(List<String> src, List<String> des) {
        if (null == src) return null == des || 0 == des.size();
        if (null == des) return 0 == src.size();

        int len = src.size() < des.size() ? src.size() : des.size();
        for (int i = 0; i<len; ++i) {
            if (!TextUtils.equals(src.get(i), des.get(i))) {
                TstRunner.print(src.get(i) + " != " + des.get(i), false);
            }
        }
        if (src.size() < des.size()) {
            for (; len < des.size(); ++len) {
                TstRunner.print("more info: " + des.get(len), false);
            }
        } else if (src.size() > des.size()) {
            for (; len < src.size(); ++len) {
                TstRunner.print("miss info: " + src.get(len), false);
            }
        }
        return src.size() == des.size();
    }

    public void testGetprop() {
        try {
            ShellUtils.ShellResult sr = ShellUtils.execute("getprop ro.debuggable");
            String out = sr.getOutput().trim();
            String err = sr.getErrput().trim();
            if (!TextUtils.isEmpty(err)) {
                TstRunner.print("getprop dbg err: " + err, false);
            }
            TstRunner.print("getprop dbg: " + out, TextUtils.equals(out, "0"));
        } catch (Throwable e) {
            e.printStackTrace();
            TstRunner.print("getprop except: " + e.getMessage(), false);
        }
    }

    public void testCmd() {
        try {
            ShellUtils.ShellResult sr = ShellUtils.execute("ls -a /data/data/" + FireyerCaseConsts.PACKAGE_NAME);
            String[] lines = sr.getOutput().split("\n");
            for (String line : lines) {
                line = line.trim();
                if (TextUtils.isEmpty(line) || line.equals(".") || line.equals("..")) continue;
                if (line.startsWith(".")) {
                    TstRunner.print("found hide: " + line, false);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            TstRunner.print("cmd except: " + e.getMessage(), false);
        }
    }
}

