package com.languo.mooccar.common.databus;

/**
 * Created by YuLiang on 2018/8/7.
 *
 * 数据订阅者 presenter 要实现这个接口来接收数据
 *
 */

public interface DataBusSubscriber {

    void onEvent(Object data);

}
