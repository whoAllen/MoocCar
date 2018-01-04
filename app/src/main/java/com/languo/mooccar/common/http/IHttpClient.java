package com.languo.mooccar.common.http;

/**
 * Created by Answer on 2017/11/19.
 * 抽象接口，post 和 get 方法
 */

public interface IHttpClient {
    IResponse get(IRequest request, boolean forceCache);

    IResponse post(IRequest request, boolean forceCache);
}
