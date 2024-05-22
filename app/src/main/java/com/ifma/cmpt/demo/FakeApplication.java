package com.ifma.cmpt.demo;

import android.app.Application;
import android.content.Context;

import com.ifma.cmpt.demo.test.FireyerRuntimeCase;

public class FakeApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        FireyerRuntimeCase.sFakeApplicationABC = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FireyerRuntimeCase.sFakeApplicationCreate = false;
    }

}
