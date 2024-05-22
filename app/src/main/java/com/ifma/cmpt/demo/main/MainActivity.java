package com.ifma.cmpt.demo.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.ifma.cmpt.demo.fireyer.BuildConfig;
import com.ifma.cmpt.demo.fireyer.R;
import com.ifma.cmpt.demo.test.FireyerCaseConsts;
import com.ifma.cmpt.demo.test.FireyerPackageCase;
import com.ifma.cmpt.demo.test.FireyerRuntimeCase;
import com.ifma.cmpt.demo.test.FireyerStackCase;
import com.ifma.cmpt.testin.env.TstConsts;
import com.ifma.cmpt.testin.env.TstHandler;
import com.ifma.cmpt.testin.module.TstPermissionGrantor;
import com.ifma.cmpt.utils.AssetsUtils;

import java.io.File;
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
        TstHandler.post(new Runnable() {
            @Override
            public void run() {
                FireyerRuntimeCase.dumpThread();
                FireyerPackageCase.dumpApk(getApplicationContext());
                Toast.makeText(getApplicationContext(), "Init done", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
