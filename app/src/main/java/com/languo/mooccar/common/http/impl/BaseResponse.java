package com.languo.mooccar.common.http.impl;

import com.languo.mooccar.common.http.IResponse;

/**
 * Created by Answer on 2017/11/19.
 */

public class BaseResponse implements IResponse {
    public static final int STATE_UNKNOWN_ERROR = 100001;
    public static final int STATE_OK = 200;
    //状态码
    private int code;
    //响应数据
    private String data;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

     public void setCode(int code) {
         this.code = code;
     }
}
