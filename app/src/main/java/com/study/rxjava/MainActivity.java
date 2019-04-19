package com.study.rxjava;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    //入门
    public void studySimple(View view) {
        RxJavaHelper.getHelper().studySimple();
    }

    //体验链式编程  chain 锁链
    public void studyChain(View view) {
        RxJavaHelper.getHelper().studyChain();
    }


    //线程切换
    public void threadChange(View view) {
        RxJavaHelper.getHelper().studyThread();
    }

    //map操作符
    public void mapStudy(View view) {
        RxJavaHelper.getHelper().studyMap();
    }

    public void flatmapStudy(View view) {
        RxJavaHelper.getHelper().studyFlatMap();
    }
}
