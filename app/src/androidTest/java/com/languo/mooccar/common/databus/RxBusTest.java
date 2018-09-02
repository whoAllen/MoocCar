package com.languo.mooccar.common.databus;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.reactivex.functions.Function;

/**
 * Created by YuLiang on 2018/8/28.
 */


public class RxBusTest {

    public static final String TAG = "RxBusTest";
    private Presenter presenter;

    @Before
    public void setUp() {
        presenter = new Presenter(new Manager());
        RxBus.getInstance().register(presenter);
    }

    @After
    public void tearDown() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RxBus.getInstance().unRegister(presenter);
    }

    @Test
    public void testGetUser() {
        presenter.getUser();
    }

}

class Presenter {
    private Manager manager;

    public Presenter(Manager manager) {
        this.manager = manager;
    }

    public void getUser() {
        manager.getUser();
    }

    public void getOrder() {
        manager.getOrder();
    }

//    @Override
//    public void onEvent(Object data) {
//        if(data instanceof User) {
//            Log.i(RxBusTest.TAG, "onEvent: Receive User in thread:" + Thread.currentThread().getName());
//        } else if(data instanceof Order) {
//            Log.i(RxBusTest.TAG, "onEvent: Receive Order in thread:" + Thread.currentThread().getName());
//        } else {
//            Log.i(RxBusTest.TAG, "onEvent: Receive Data in Thread:" + Thread.currentThread().getName());
//        }
//    }

    @RegisterBus
    public void onUser(User user) {
        Log.i(RxBusTest.TAG, "onEvent: Receive User in thread:" + Thread.currentThread().getName());
    }

    public void onOrder(Order order) {
        Log.i(RxBusTest.TAG, "onEvent: Receive Order in thread:" + Thread.currentThread().getName());
    }

}

/**
 * 模拟 Model
 */
class Manager {

    public void getUser() {
        RxBus.getInstance().chainProcess(new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                Log.i(RxBusTest.TAG, "chainProcess getUser start in thread: " + Thread.currentThread().getName());
                User user = new User();
                Thread.sleep(1000);
                return user;
            }
        });
    }

    public void getOrder() {
        RxBus.getInstance().chainProcess(new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                Log.i(RxBusTest.TAG, "chainProgress getOrder start in thread: " + Thread.currentThread().getName());
                Order order = new Order();
                Thread.sleep(1000);
                return order;
            }
        });
    }
}

class User {

}

class Order {

}