package com.example.event;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by suhu on 2017/7/6.
 */

public class MyApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        LeakCanary.install(this);
    }
}
