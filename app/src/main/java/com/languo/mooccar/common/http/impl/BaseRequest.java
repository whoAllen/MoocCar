package com.languo.mooccar.common.http.impl;

import com.google.gson.Gson;
import com.languo.mooccar.common.http.IRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Answer on 2017/11/19.
 *
 * 封装参数的实现
 */

public class BaseRequest implements IRequest {

    private String method = POST;
    private String url;
    private Map<String, String> header;
    private Map<String, Object> body;

    /**
     * 公共参数和头部信息
     * @param url
     */
    public BaseRequest(String url) {
        this.url = url;
        header = new HashMap<>();
        body = new HashMap<>();
        header.put("Application-Id", "myTaxiID");
        header.put("API-Key", "myTaxiKey");
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public void setHeader(String key, String value) {
        header.put(key, value);
    }

    @Override
    public void setBody(String key, String value) {
        body.put(key, value);
    }

    @Override
    public String getUrl() {
        if(GET.equals(url)) {
            //组装 GET 请求参数
            for(String key : body.keySet()) {
                url = url.replace("${" + key + "}", body.get(key).toString());
            }
        }
        return url;
    }

    @Override
    public Map<String, String> getHeader() {
        return header;
    }

    @Override
    public Object getBody() {
        if (body != null) {
            //POST 请求方式
            return new Gson().toJson(body, HashMap.class);
        } else {
            return "{}";
        }

    }
}
