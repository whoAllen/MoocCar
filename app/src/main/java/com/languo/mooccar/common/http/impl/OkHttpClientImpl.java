package com.languo.mooccar.common.http.impl;

import android.app.DownloadManager;
import android.telecom.Call;

import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.IRequest;
import com.languo.mooccar.common.http.IResponse;

import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Answer on 2017/11/19.
 */

public class OkHttpClientImpl implements IHttpClient {

    private OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    @Override
    public IResponse get(IRequest request, boolean forceCache) {
        /**
         * 解析业务参数
         */
        //指定请求方式
        request.setMethod(IRequest.GET);
        //OKHttp 的 Request.Builder
        Request.Builder builder = new Request.Builder();
        //解析头部
        Map<String, String> header = request.getHeader();
        for(String key : header.keySet()) {
            builder.header(key, header.get(key));
        }
        //获取 url
        String url = request.getUrl();
        builder.url(url)
                .get();
        Request okRequest = builder.build();
        return execute(okRequest);
    }

    @Override
    public IResponse post(IRequest request, boolean forceCache) {
        request.setMethod(IRequest.POST);
        //OkHttp 的 Request.Builder
        Request.Builder builder = new Request.Builder();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, request.getBody().toString());
        Map<String, String> header = request.getHeader();
        for(String key : header.keySet()) {
            builder.header(key, header.get(key));
        }
        String url = request.getUrl();
        builder.url(url)
                .post(body);
        Request okRequest = builder.build();

        return execute(okRequest);
    }

    private IResponse execute(Request request) {
        BaseResponse commonResponse = new BaseResponse();
        try {
            //发送请求
            Response response = okHttpClient.newCall(request).execute();
            //设置响应码和响应数据
            commonResponse.setCode(response.code());
            commonResponse.setData(response.body().string());

        } catch (IOException e) {
            e.printStackTrace();
            commonResponse.setCode(commonResponse.STATE_UNKNOWN_ERROR);
            commonResponse.setData(e.getMessage());
        }
        return commonResponse;
    }
}
