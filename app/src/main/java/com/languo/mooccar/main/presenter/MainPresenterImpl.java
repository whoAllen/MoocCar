package com.languo.mooccar.main.presenter;

import android.os.Handler;
import android.os.Message;

import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.model.response.LoginResponse;
import com.languo.mooccar.common.databus.RegisterBus;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.main.view.IMainView;

import java.lang.ref.WeakReference;

/**
 * Created by YuLiang on 2018/3/3.
 */

public class MainPresenterImpl implements IMainPresenter {

    private IMainView view;
    private IAccountManager accountManager;

    public MainPresenterImpl(IMainView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
    }

    @RegisterBus
    public void onLoginResponse(LoginResponse loginResponse) {
        switch (loginResponse.getCode()) {
            case IAccountManager.LOGIN_SUC:
                //登录成功
               view.showLoginSuc();
                break;
            case IAccountManager.SERVER_FAIL:
                //服务器错误
                view.showError(IAccountManager.SERVER_FAIL, "");
                break;
            case IAccountManager.TOKEN_INVALID:
                //token 过期
                view.showError(IAccountManager.SERVER_FAIL, "");
                break;
        }
    }

    @Override
    public void loginByToken() {
        accountManager.loginByToken();
    }
}
