package com.example.event;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestActivity extends AppCompatActivity {

    @BindView(R.id.textView)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new Message("test ", 22));

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                EventBus.getDefault().post(new Message("hh", 22));
//            }
//        }).start();

    }



    /**
     * 解决问题：
     * 1.刚开始子线程可以更新UI,过一会就不能更新UI
     * 在onCreate中发送消息
     * EventBus.getDefault().post(new Message("test ", 22));
     * 可以在子线程可以更新UI(而且不会抛出异常)
     * 原因在于，此时ViewRootImpl类还未被创建，无法调用checkThread()方法检测是否是UI线程，所以可以更新UI
     * 当点击button后再发送消息后
     * @Subscribe(threadMode = ThreadMode.ASYNC)
     * public void messages(final Message message){}
     * 获得到对象在子线程更新UI,此时ViewRootImpl对象已经被创建，调用checkThread()判断，执行textView.setText(message.toString())
     * 在哪个线程中，当检测到不是主线程就直接抛出异常CalledFromWrongThreadException
     *
     * 2.Toast并不是一定非要在主线程
     * 在调用 show()方法的时候需要一个TN tn = mTN对象，创建TN对象的时候需要创健Handler对象，但是此时没有创健Looper
     * 为什么没有创健Looper呢？
     * 因为主线程已经帮助我们创健好Looper，所以不用我们自己创健，那么问题来了，当没有Looper对象的时候Toast就用不了
     * 也就是在子线程中使用Toast的时候需要我们手动创健一个Looper对象
     *
     *
     * */




    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void messages(final Message message) {

        Log.i("TestActivity", Thread.currentThread().getName());
        message.toString();
        textView.setText(message.toString());


//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(TestActivity.this,message.toString(),Toast.LENGTH_LONG).show();
//            }
//        });

//        Looper.prepare();
//        Toast.makeText(TestActivity.this,message.toString(),Toast.LENGTH_LONG).show();
//        Looper.loop();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(TestActivity.this,message.toString(),Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @OnClick(R.id.button2)
    public void onViewClicked() {
        EventBus.getDefault().post(new Message("send",20));
        //finish();
    }
}
