package com.example.suhu.rxjavadome;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

//https://mp.weixin.qq.com/s?__biz=MzIwMzYwMTk1NA==&mid=2247484939&idx=1&sn=d1871b09de55ca681da6ec2432a63ee1&chksm=96cda746a1ba2e501605dbb64b32f82b2cb44ad9c6f9ba2308a9321231b2782e6ba679ef31a6&mpshare=1&scene=23&srcid=0614bARC7g4dwQ3qbIVEJ29b#rd&ref=myread
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.textView2)
    TextView textView2;
    @BindView(R.id.textView3)
    TextView textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        /**
         * 当上游发送了一个onError后, 上游onError之后的事件将继续发送,
         * 而下游收到onError事件之后将不再继续接收事件.
         *
         * */
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                //发射器 无限次发送
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                //结束标示
                e.onComplete();
                String thread = Thread.currentThread().getName();

            }
        })

                .subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
               // .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Integer integer) {
                String thread = Thread.currentThread().getName();
                switch (integer){
                    case 1:
                        textView.setText(integer+"");
                        break;
                    case 2:
                        textView2.setText(integer+"");
                        break;
                    case 3:
                        textView3.setText(integer+"");
                        break;
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });




    }
}
