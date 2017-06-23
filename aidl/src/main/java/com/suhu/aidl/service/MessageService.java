package com.suhu.aidl.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.suhu.aidl.MessageReceiver;
import com.suhu.aidl.MessageSender;
import com.suhu.aidl.data.MessageModel;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by suhu on 2017/6/22.
 */

public class MessageService extends Service {
    private static final String TAG = "MessageService";
    private AtomicBoolean serviceStop = new AtomicBoolean(false);
    //RemoteCallbackList专门用来管理多进程回调接口
    private RemoteCallbackList<MessageReceiver> listenerList = new RemoteCallbackList<>();


    public MessageService() {
    }


    IBinder messageSender = new MessageSender.Stub() {
        @Override
        public void sendMessage(MessageModel messageModel) throws RemoteException {
            Log.i(TAG, "sendMessage=" + messageModel.toString());
        }

        @Override
        public void registerReceiveListener(MessageReceiver messageReceiver) throws RemoteException {
            listenerList.register(messageReceiver);
        }

        @Override
        public void unregisterReceiveListener(MessageReceiver messageReceiver) throws RemoteException {
            listenerList.unregister(messageReceiver);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            /**
             * 包名验证方式
             */
            String packageName = null;
            String[] packages = getPackageManager().getPackagesForUid(getCallingUid());
            if (packages != null && packages.length > 0) {
                packageName = packages[0];
            }
            if (packageName == null || !packageName.startsWith("com.suhu.aidl")) {
                Log.i(TAG, "onTransact+拒绝调用:" + packageName);
                return false;
            }

            return super.onTransact(code, data, reply, flags);
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        //自定义permission方式检查权限
        if (checkCallingOrSelfPermission("com.suhu.aidl.permission.REMOTE_SERVICE_PERMISSION")== PackageManager.PERMISSION_DENIED){
            return null;
        }
        return messageSender;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new FakeTCPTask()).start();
    }

    @Override
    public void onDestroy() {
        serviceStop.set(true);
        super.onDestroy();
    }

    private class FakeTCPTask implements Runnable{

        @Override
        public void run() {
            int i = 0;
            while (!serviceStop.get()){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MessageModel messageModel = new MessageModel();
                messageModel.setFrom("服务端");
                messageModel.setTo("客户端");
                messageModel.setContent("这是第"+i+"次给你发送东西");
                i++;

                Log.i(TAG, "FakeTCPTask=:" + messageModel.toString());

                /**
                 * RemoteCallbackList的遍历方式
                 * beginBroadcast和finishBroadcast一定要配对使用
                 */
                final  int listenerCount = listenerList.beginBroadcast();
                Log.i(TAG,"listenerCount=="+listenerCount);
                for (int i1 = 0; i1 < listenerCount; i1++) {
                    MessageReceiver messageReceiver = listenerList.getBroadcastItem(i1);
                    if (messageReceiver !=null){
                        try {
                            messageReceiver.onMessageReceived(messageModel);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                if (listenerCount==0){
                    Intent intent = MessageService.this.getPackageManager().getLaunchIntentForPackage("com.suhu.aidl");
                    startActivity(intent);
                }
                listenerList.finishBroadcast();

            }
        }
    }

}
