package com.languo.mooccar.account.presenter;

/**
 * Created by YuLiang on 2018/3/3.
 */

public interface ISmsCodeDialogPresenter {
    /**
     * 请求下发验证码
     */

    void requestSendSmsCode(String phone);

    /**
     * 请求验证验证码
     */
    void requestCheckSmsCode(String phone, String password);

    /**
     * 用户是否存在
     */
    void requestCheckUserExist(String phone);
}
