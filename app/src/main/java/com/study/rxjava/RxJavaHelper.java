package com.study.rxjava;


import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.study.rxjava.request.LoginRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * author : fflin
 * date   : 2019/4/19 10:08
 * desc   : 学习rxJava，学习地址https://www.jianshu.com/p/464fa025229e
 * version: 1.0
 */
public class RxJavaHelper {

    private static final String TAG = "RxJavaLog";
    private static RxJavaHelper helper;

    private RxJavaHelper() {

    }

    public static RxJavaHelper getHelper() {
        synchronized (RxJavaHelper.class) {
            if (helper == null) {
                helper = new RxJavaHelper();
            }
        }
        return helper;
    }


    //第一段代码，了解rxJava的发布订阅模式
    public void studySimple() {
        //1 创建上游被观察者
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                //1.1 被观察者发送3个事件
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                //1.2 被观察这调用发送完成的方法
                emitter.onComplete();
            }
        });

        //2 创建下游观察者，监听上游变化
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                LogHelper.i(TAG, "onSubscribe-------");
            }

            @Override
            public void onNext(Integer value) {
                LogHelper.i(TAG, "onNext-----:" + value);
            }

            @Override
            public void onError(Throwable e) {
                LogHelper.i(TAG, "onError----------");
            }

            @Override
            public void onComplete() {
                LogHelper.i(TAG, "onComplete-----------");
            }
        };

        //3 重要！！！  上下游建立连接
        observable.subscribe(observer);
    }

    //第二段，熟悉链式编程，直接将上下游连接起来
    public void studyChain() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            private Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = d;
                LogHelper.i(TAG, "onSubscribe-----------");
            }

            @Override
            public void onNext(Integer value) {
                LogHelper.i(TAG, "onNext-------value = " + value);
                if (value == 2)
                    disposable.dispose();//中断连接的方法,中断以后，不会中断上游发送剩余事件，但是下游不再接收事件******************
            }

            @Override
            public void onError(Throwable e) {
                LogHelper.i(TAG, "onError--------------" + e.getMessage());
            }

            @Override
            public void onComplete() {
                LogHelper.i(TAG, "onComplete-------------");
            }
        });
    }

    //线程切换学习
    public void studyThread() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                LogHelper.i(TAG, "Observable  thread : " + Thread.currentThread().getName());//以声明的最后一次为准
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())//指定上游线程，多次指定以第一次为准
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())//指定下游线程，多次指定以第一次为准
                .observeOn(Schedulers.newThread())

                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        LogHelper.i(TAG, "onSubscribe-----------");
                    }

                    @Override
                    public void onNext(Integer value) {
                        LogHelper.i(TAG, "Observer  thread : " + Thread.currentThread().getName() + "; " + value);//调用线程main，以声明的第一次为准
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogHelper.i(TAG, "onError--------------" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        LogHelper.i(TAG, "onComplete-------------");
                    }
                });
    }

    //map操作符，可以重新组装数据，通过这个操作符转换类型
    public void studyMap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);//发送的int类型的数据
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
            }
        }).map(new Function<Integer, String>() {//map操作符，转换类型成String
            @Override
            public String apply(Integer integer) throws Exception {
                return "map function value = "+integer;
            }
        }).subscribe(new Observer<String>() {//接收到的数据成为string类型
            @Override
            public void onSubscribe(Disposable d) {
                LogHelper.i(TAG, "onSubscribe-----------");
            }

            @Override
            public void onNext(String value) {
                LogHelper.i(TAG, "onNext ----------- "+value);
            }

            @Override
            public void onError(Throwable e) {
                LogHelper.i(TAG, "onError--------------" + e.getMessage());
            }

            @Override
            public void onComplete() {
                LogHelper.i(TAG, "onComplete-------------");
            }
        });

    }

    //flatMap将一个被观察者拆分为多个，然后再将他们合并成一个发送给下游，但不能保证事件顺序
    //虽然上游只发送了3个事件，但是通过flatMap的for循环3次后，下游接收了9次事件
    public void studyFlatMap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                LogHelper.i(TAG,"onNext 1");
                e.onNext(2);
                LogHelper.i(TAG,"onNext 2");
                e.onNext(3);
                LogHelper.i(TAG,"onNext 3");
                e.onComplete();
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                List list = new ArrayList();
                for (int i = 0; i < 3; i ++){
                    list.add("this is :"+i);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                LogHelper.i(TAG, "accept------ : "+s);
            }
        });
    }

    //使用FlatMap模拟登录注册请求
    public void LoginTest() {
        Api api = create().create(Api.class);
//        api.login(new LoginRequest()).de//todo 这里没搞完

    }

    private Retrofit create() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.connectTimeout(9, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        return new Retrofit.Builder().baseUrl("")//url
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
}
