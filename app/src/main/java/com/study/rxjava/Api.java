package com.study.rxjava;

import com.study.rxjava.request.LoginRequest;
import com.study.rxjava.request.RegisterRequest;
import com.study.rxjava.response.LoginResponse;
import com.study.rxjava.response.RegisterResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;

/**
 * author : fflin
 * date   : 2019/4/19 17:23
 * desc   : 模拟登录注册
 * version: 1.0
 */
public interface Api {
    @GET
    Observable<LoginResponse> login(@Body LoginRequest request);

    @GET
    Observable<RegisterResponse> register(@Body RegisterRequest request);
}