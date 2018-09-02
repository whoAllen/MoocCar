package com.languo.mooccar.account.presenter;


import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.model.response.LoginResponse;
import com.languo.mooccar.account.view.ILoginDialogView;
import com.languo.mooccar.common.databus.RegisterBus;

/**
 * Created by YuLiang on 2018/3/3.
 */

public class LoginDialogPresenterImpl implements ILoginDialogPresenter {
    private static final String TAG = "LoginDialogPresenterImp";

    private ILoginDialogView view;
    private IAccountManager accountManager;

    public LoginDialogPresenterImpl(ILoginDialogView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
    }

    @Override
    public void requestLogin(final String phone, final String pw) {
        accountManager.login(phone, pw);
    }

    @RegisterBus
    public void onLoginResponse(LoginResponse loginResponse) {
        switch (loginResponse.getCode()) {
            case IAccountManager.LOGIN_SUC:
                view.showLoginSuc();
                break;
            case IAccountManager.PW_ERROR:
                view.showError(IAccountManager.PW_ERROR, "密码错误");
                break;
            case IAccountManager.SERVER_FAIL:
                view.showError(IAccountManager.SERVER_FAIL, "服务器错误");
                break;
        }
    }
}
