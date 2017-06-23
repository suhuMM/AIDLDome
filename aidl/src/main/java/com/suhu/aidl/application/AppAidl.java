package com.suhu.aidl.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.util.List;

/**
 * Created by suhu on 2017/6/22.
 */

public class AppAidl extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("process name",getProcessName());
    }


    private String getProcessName() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runProcessInfo = activityManager.getRunningAppProcesses();
        if (runProcessInfo == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : runProcessInfo) {
            if (processInfo.pid == Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }


}
