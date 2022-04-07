package com.huantansheng.easyphotos.demo;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * 主要用于检测内存泄漏
 * Created by huan on 2018/1/30.
 */

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "4c251b8f40", false);
    }
}
