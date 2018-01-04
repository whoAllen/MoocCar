package com.languo.mooccar.common.http;

/**
 * Created by Answer on 2017/11/19.
 * 请求响应结果
 *
 * 状态码和数据体
 */

public interface IResponse {
    //状态码
    int getCode();
    //数据体
    String getData();
}
