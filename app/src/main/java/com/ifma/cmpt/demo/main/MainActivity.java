package com.ifma.cmpt.demo.main;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.ifma.cmpt.demo.fireyer.BuildConfig;
import com.ifma.cmpt.demo.fireyer.R;
import com.ifma.cmpt.demo.test.FireyerCaseConsts;
import com.ifma.cmpt.demo.test.FireyerStackCase;
import com.ifma.cmpt.testin.env.TstConsts;
import com.ifma.cmpt.testin.module.TstPermissionGrantor;
import com.ifma.cmpt.utils.AssetsUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
//import com.ifma.cmpt.fireyer.FireyerNative;


public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        FireyerStackCase.dumpStackForActivity_onCreate();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        TstPermissionGrantor.grant(this);
        initData();
    }

    private void initView() {
        TextView text = (TextView) findViewById(R.id.main_version);
        text.setText("v" + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")");
        View v;
        v = findViewById(R.id.item_unittest_source);
//        v.setVisibility(View.GONE);
        v.setOnClickListener(this);

        v = findViewById(R.id.item_unittest_konker);
//        v.setVisibility(View.GONE);
        v.setOnClickListener(this);

        v = findViewById(R.id.item_shared);
//        v.setVisibility(View.GONE);
        v.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.item_unittest_source:
        {
            FireyerCaseConsts.setMode(FireyerCaseConsts.MODE_SOURCE);
            Intent i = new Intent(this, ConsoleActivity.class);
            i.putExtra(TstConsts.KEY_TYPE, TstConsts.VALUE_UNITTEST);
            startActivity(i);
        }   break;
        case R.id.item_unittest_konker:
        {
            FireyerCaseConsts.setMode(FireyerCaseConsts.MODE_KONKER);
            Intent i = new Intent(this, ConsoleActivity.class);
            i.putExtra(TstConsts.KEY_TYPE, TstConsts.VALUE_UNITTEST);
            startActivity(i);
        }   break;
        case R.id.item_shared:
        {
            File file = new File(getExternalCacheDir(), "test.pdf");
            if (AssetsUtils.copyAsset(getApplicationContext(), "test.pdf", file.getAbsolutePath())) {
                openFileWithInstalledApps(getApplicationContext(), file, "application/pdf");
            } else {
                Toast.makeText(getApplicationContext(), "test.pdf export fail", Toast.LENGTH_SHORT).show();
            }
        }   break;
        default: break;
        }
    }

    public void openFileWithInstalledApps(Context context, File file, String mimeType) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".SubProvider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void initData() {
        initUsbPermission();
//        TstHandler.post(new Runnable() {
//            @Override
//            public void run() {
//            }
//        });
    }

    private static final String ACTION_USB_PERMISSION_DEVICE = "com.ifma.cmpt.demo.fireyer.USB_PERMISSION_DEVICE";
    private static final String ACTION_USB_PERMISSION_ACCESSORY = "com.ifma.cmpt.demo.fireyer.USB_PERMISSION_ACCESSORY";

    protected void initUsbPermission() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION_DEVICE);
        filter.addAction(ACTION_USB_PERMISSION_ACCESSORY);
        registerReceiver(usbReceiver, filter);

        PendingIntent intent = PendingIntent.getBroadcast(this, 0,
            new Intent(ACTION_USB_PERMISSION_DEVICE), 0
        );

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        if (null != deviceList && 0 < deviceList.size()) {
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                boolean has = usbManager.hasPermission(device);
                Toast.makeText(getApplicationContext(), "usb-dev-pmt: " + has, Toast.LENGTH_SHORT).show();
                if (!has) {
                    usbManager.requestPermission(device, intent);
                }
            }
        }

        intent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION_ACCESSORY), 0
        );
        UsbAccessory[] accs = usbManager.getAccessoryList();
        if (null != accs && 0 < accs.length) {
            for (UsbAccessory acc: accs) {
                boolean has = usbManager.hasPermission(acc);
                Toast.makeText(getApplicationContext(), "usb-acc-pmt: " + has, Toast.LENGTH_SHORT).show();
                if (!has) {
                    usbManager.requestPermission(acc, intent);
                }
            }
        }
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION_DEVICE.equals(action)) {
                UsbDevice dev = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Toast.makeText(getApplicationContext(), "usb-dev: " + dev.getSerialNumber(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "usb-dev: fail", Toast.LENGTH_SHORT).show();
                }
            } else if (ACTION_USB_PERMISSION_ACCESSORY.equals(action)) {
                UsbAccessory acc = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Toast.makeText(getApplicationContext(), "usb-acc: " + acc.getSerial(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "usb-dev: fail", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    protected void onDestroy() {
        unregisterReceiver(usbReceiver);
        super.onDestroy();
    }

}
