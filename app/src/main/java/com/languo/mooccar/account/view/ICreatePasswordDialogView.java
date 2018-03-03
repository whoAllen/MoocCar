package com.languo.mooccar.account.view;

/**
 * Created by YuLiang on 2018/3/3.
 */

public interface ICreatePasswordDialogView extends IView{

    /**
     * 显示注册成功
     */
    void showRegisterSuc();

    /**
     * 显示登录成功
     */
    void showLoginSuc();

    /**
     * 显示密码为空
     */
    void showPasswordNull();

    /**
     * 显示两次输入的密码不一样
     */
    void showPasswordNotEqual();

}
