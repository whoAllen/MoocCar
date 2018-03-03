package com.languo.mooccar.account.presenter;

import android.accounts.AccountManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.languo.mooccar.MoocCarApplication;
import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.model.response.LoginResponse;
import com.languo.mooccar.account.view.ILoginDialogView;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.IRequest;
import com.languo.mooccar.common.http.IResponse;
import com.languo.mooccar.common.http.api.API;
import com.languo.mooccar.common.http.biz.BaseBizResponse;
import com.languo.mooccar.common.http.impl.BaseRequest;
import com.languo.mooccar.common.http.impl.OkHttpClientImpl;
import com.languo.mooccar.common.storage.SharedPreferencesDao;

import java.lang.ref.WeakReference;

/**
 * Created by YuLiang on 2018/3/3.
 */

public class LoginDialogPresenterImpl implements ILoginDialogPresenter {
    private static final String TAG = "LoginDialogPresenterImp";

    private ILoginDialogView view;
    private IAccountManager accountManager;

    private static class MyHandler extends Handler {
        WeakReference<LoginDialogPresenterImpl> refContext;

        public MyHandler(LoginDialogPresenterImpl context) {
            refContext = new WeakReference(context);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginDialogPresenterImpl presenter = refContext.get();
            super.handleMessage(msg);
            switch (msg.what) {
                case IAccountManager.LOGIN_SUC:
                    presenter.view.showLoginSuc();
                    break;
                case IAccountManager.PW_ERROR:
                    presenter.view.showError(IAccountManager.PW_ERROR, "");
                    break;
                case IAccountManager.SERVER_FAIL:
                    presenter.view.showError(IAccountManager.SERVER_FAIL, "");
                    break;
            }
        }
    }

    public LoginDialogPresenterImpl(ILoginDialogView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;

        accountManager.setHandler(new MyHandler(this));
    }

    @Override
    public void requestLogin(final String phone, final String pw) {
        accountManager.login(phone, pw);
    }
}
