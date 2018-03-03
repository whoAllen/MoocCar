package com.languo.mooccar.account.view;

/**
 * Created by YuLiang on 2018/3/3.
 */

public interface IView {
    /**
     * 显示等待
     */
    void showLoading();

    /**
     * 显示错误
     */
    void showError(int code, String msg);
}
