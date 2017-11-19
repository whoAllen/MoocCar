package com.languo.mooccar;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Answer on 2017/11/12.
 */

public class TestOkHttp3 {

    /**
     * 测试 OkHttp GET 请求
     */
    @Test
    public void testGet() {
        //创建 OkHttpClient 对象
        OkHttpClient client = new OkHttpClient();
        //创建 Request 对象
        Request request = new Request.Builder()
                .url("http://httpbin.org/get?id==id")
                .build();
        //OkHttpClient 执行 Request
        try {
            Response response = client.newCall(request).execute();
            System.out.println("response: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试 OkHttp POST 请求
     */
    @Test
    public void testPost() {
        //创建 OkHttpClient 对象
        OkHttpClient client = new OkHttpClient();
        //创建 Request 对象
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, "{\"name\":\"yuliang\"}");
        Request request = new Request.Builder()
                .url("http://httpbin.org/post")//请求行
//                .addHeader()//请求头
                .post(requestBody)//请求体
                .build();
        //OkHttpClient 执行 Request
        try {
            Response response = client.newCall(request).execute();
            System.out.println("response: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试拦截器
     * 拦截器可以拦截请求的发送和响应
     */
    @Test
    public void testInterceptor() {
        //定义拦截器
        final Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                long start = System.currentTimeMillis();
                Request request = chain.request();
                Response response = chain.proceed(request);
                long end = System.currentTimeMillis();
                System.out.println("interceptor: cost time " + (end - start));
                return response;
            }
        };

        //使用拦截器
        //创建 OkHttpClient 对象
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)//添加拦截器
                .build();
        //创建 Request 对象
        Request request = new Request.Builder()
                .url("http://httpbin.org/get?id==id")
                .build();
        //OkHttpClient 执行 Request
        try {
            Response response = client.newCall(request).execute();
            System.out.println("response: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试缓存
     */
    @Test
    public void testCache() {
        //创建缓存对象
        Cache cache = new Cache(new File("cache.cache"), 1024 * 1024);

        //创建 OkHttpClient 对象
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)//添加缓存
                .build();
        //创建 Request 对象
        Request request = new Request.Builder()
                .url("http://httpbin.org/get?id==id")
                .cacheControl(CacheControl.FORCE_CACHE)//缓存控制：设置请求从缓存中读取数据还是从网络中读取数据
                .build();
        //OkHttpClient 执行 Request
        try {
            Response response = client.newCall(request).execute();
            //获得缓存
            Response responseCache = response.cacheResponse();
            Response responseNet = response.networkResponse();
            if(responseNet != null) {
                //从网络响应
                System.out.println("response from net");
            }
            if(responseCache != null) {
                //从缓存响应
                System.out.println("response from cache : "  + responseCache.body().string());
            }
            System.out.println("response: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
