package com.languo.mooccar.account.presenter;


import android.text.TextUtils;

import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.model.response.LoginResponse;
import com.languo.mooccar.account.model.response.RegisterResponse;
import com.languo.mooccar.account.view.ICreatePasswordDialogView;
import com.languo.mooccar.common.databus.RegisterBus;

/**
 * Created by YuLiang on 2018/3/3.
 */

public class CreatePasswordDialogPresenterImpl implements ICreatePasswordDialogPresenter {

    private IAccountManager accountManager;
    private ICreatePasswordDialogView view;


    public CreatePasswordDialogPresenterImpl(IAccountManager accountManager, ICreatePasswordDialogView view) {
        this.accountManager = accountManager;
        this.view = view;
    }

    @Override
    public boolean checkPw(String pw, String pw1) {

        if(TextUtils.isEmpty(pw)) {
            //密码为空，提示
            view.showPasswordNull();
            return false;
        }
        if(!pw.equals(pw1)) {
            view.showPasswordNotEqual();
            return false;
        }
        return true;
    }

    @Override
    public void register(String phone, String password) {
        accountManager.register(phone, password);
    }

    @Override
    public void login(String phone, String password) {
        accountManager.login(phone, password);
    }

    @RegisterBus
    public void onRegisterResponse(RegisterResponse registerResponse) {
        switch (registerResponse.getCode()) {
            case IAccountManager.REGISTER_SUC:
                view.showRegisterSuc();
                break;
            case IAccountManager.LOGIN_SUC:
                view.showLoginSuc();
                break;
            case IAccountManager.SERVER_FAIL:
                view.showError(registerResponse.getCode(), registerResponse.getMsg());
                break;
        }
    }

    @RegisterBus
    public void onLoginResponse(LoginResponse loginResponse) {
        switch (loginResponse.getCode()) {
            case IAccountManager.REGISTER_SUC:
                view.showRegisterSuc();
                break;
            case IAccountManager.LOGIN_SUC:
                view.showLoginSuc();
                break;
            case IAccountManager.SERVER_FAIL:
                view.showError(loginResponse.getCode(), loginResponse.getMsg());
                break;
        }
    }
}
