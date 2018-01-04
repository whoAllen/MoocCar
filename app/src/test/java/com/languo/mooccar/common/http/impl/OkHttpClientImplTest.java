package com.languo.mooccar.common.http.impl;

import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.IRequest;
import com.languo.mooccar.common.http.IResponse;
import com.languo.mooccar.common.http.api.API;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Answer on 2017/11/19.
 */
public class OkHttpClientImplTest {
    IHttpClient httpClient;
    //测试之前设置统一的配置
    @Before
    public void setUp() throws Exception {
        httpClient = new OkHttpClientImpl();
        API.Config.setDebug(false);
    }

    @Test
    public void get() throws Exception {
        // 设置 request 参数
        String url = API.Config.getDomain() + API.TEST_GET;
        IRequest request = new BaseRequest(url);
        request.setHeader("testHeader", "test header");
        request.setBody("uid", "123456");
        IResponse response = httpClient.get(request, false);
        System.out.println("stateCode = " + response.getCode());
        System.out.println("body =" + response.getData());
    }

    @Test
    public void post() throws Exception {

    }

}