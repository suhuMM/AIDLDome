package com.suhu.aidl.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.suhu.aidl.MessageReceiver;
import com.suhu.aidl.MessageSender;
import com.suhu.aidl.R;
import com.suhu.aidl.data.MessageModel;
import com.suhu.aidl.service.MessageService;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MessageSender messageSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupService();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 1.unregisterListener
     * 2.unbindService
     */
    @Override
    protected void onDestroy() {
        //解除消息监听接口
        if (messageSender!=null&&messageSender.asBinder().isBinderAlive()){
            try {
                messageSender.unregisterReceiveListener(messageReceiver);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        unbindService(serviceConnection);
        super.onDestroy();
    }

    /**
     * bindService & startService：
     * 使用bindService方式，多个Client可以同时bind一个Service，但是当所有Client unbind后，Service会退出
     * 通常情况下，如果希望和Service交互，一般使用bindService方法，获取到onServiceConnected中的IBinder对象，和Service进行交互，
     * 不需要和Service交互的情况下，使用startService方法即可，Service主线程执行完成后会自动关闭；
     * unbind后Service仍保持运行，可以同时调用bindService和startService（比如像聊天软件，退出UI进程，Service仍能接收消息）
     */
    private void setupService() {
        Intent intent = new Intent(this,MessageService.class);
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }


    /**
     * Binder可能会意外死忙（比如Service Crash），Client监听到Binder死忙后可以进行重连服务等操作
     */
    IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient(){
        @Override
        public void binderDied() {
            Log.i(TAG,"binder死亡");
            if (messageSender !=null){
                messageSender.asBinder().unlinkToDeath(this,0);
                messageSender = null;
            }
            //TODO:重连服务器
            setupService();
        }
    };


    //消息监听回调接口
    private MessageReceiver messageReceiver = new MessageReceiver.Stub(){
        @Override
        public void onMessageReceived(MessageModel receivedMessage) throws RemoteException {
            Log.i(TAG,"onMessageReceived"+receivedMessage.toString());
        }
    };


    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messageSender = MessageSender.Stub.asInterface(service);

            MessageModel messageModel = new MessageModel();
            messageModel.setFrom("客户端发送");
            messageModel.setTo("服务端接收");
            messageModel.setContent("我是客户端给你服务费发送消息，你记得接收");

            try {
                //设置Binder死亡监听
                messageSender.asBinder().linkToDeath(deathRecipient,0);
                //把接收消息的回调接口注册到服务端
                messageSender.registerReceiveListener(messageReceiver);
                //调用远程Service的sendMessage方法，并传递消息实体对象
                messageSender.sendMessage(messageModel);

                Log.i(TAG,"onServiceConnected");

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    protected void close(View v){
        finish();
    }

}
