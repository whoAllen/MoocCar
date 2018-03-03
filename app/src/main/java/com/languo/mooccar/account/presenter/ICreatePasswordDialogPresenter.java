package com.languo.mooccar.account.presenter;

/**
 * Created by YuLiang on 2018/3/3.
 */

public interface ICreatePasswordDialogPresenter {

    /**
     * 校验密码输入合法性
     */
    boolean checkPw(String pw, String pw1);

    /**
     * 提交注册
     */
    void register(String phone, String password);

    /**
     * 登录
     */
    void login(String phone, String password);

}
