package com.languo.mooccar.common.databus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by YuLiang on 2018/8/7.
 * <p>
 * 数据交互处理
 */

public class RxBus {

    //订阅者集合
    private Set<Object> subscriberSet;
    public static RxBus instance;

    /**
     * 注册
     *
     * @param subscriber
     */
    public void register(Object subscriber) {
        subscriberSet.add(subscriber);
    }

    /**
     * 取消注册
     *
     * @param subscriber
     */
    public void unRegister(Object subscriber) {
        subscriberSet.remove(subscriber);
    }

    public RxBus() {
        subscriberSet = new CopyOnWriteArraySet<>();
    }

    /**
     * 单例模式
     *
     * @return
     */
    public static RxBus getInstance() {
        if (instance == null) {
            synchronized (RxBus.class) {
                if (instance == null) {
                    instance = new RxBus();
                }

            }
        }
        return instance;
    }

    /**
     * 处理数据
     *
     * @param function
     */
    public void chainProcess(Function function) {
        Observable.just("")
                .subscribeOn(Schedulers.io())//指定处理在 IO 线程
                .map(function)// 包装处理过程
                .observeOn(AndroidSchedulers.mainThread())//指定时间消费在主线程
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object data) throws Exception {
                        for (Object subscriber : subscriberSet) {
                            //扫描注解，将数据发送到注册的对象的标记方法
                            callMethodByAnnotation(subscriber, data);
                        }
                    }
                });
    }


    /**
     * 反射获取对象方法列表，判断：
     * 1 是否被注解修饰
     * 2 参数类型是否和 data 类型一致
     *
     * @param target
     * @param data
     */
    private void callMethodByAnnotation(Object target, Object data) {
        Method[] methodArray = target.getClass().getDeclaredMethods();
        for (int i = 0; i < methodArray.length; i++) {
            try {
                if (methodArray[i].isAnnotationPresent(RegisterBus.class)) {
                    //被 @Register 修饰的方法
                    Class paramType = methodArray[i].getParameterTypes()[0];
                    if (data.getClass().getName().equals(paramType.getName())) {
                        //参数类型和 data 一样，调用方法
                        methodArray[i].invoke(target, new Object[]{data});
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
