package com.languo.mooccar.main.presenter;

import android.os.Handler;
import android.os.Message;

import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.main.view.IMainView;

import java.lang.ref.WeakReference;

/**
 * Created by YuLiang on 2018/3/3.
 */

public class MainPresenterImpl implements IMainPresenter {

    private IMainView view;
    private IAccountManager accountManager;

    static class MyHandler extends Handler {
        WeakReference<MainPresenterImpl> refContext;

        public MyHandler(MainPresenterImpl mContext) {
            refContext = new WeakReference<MainPresenterImpl>(mContext);
        }

        @Override
        public void handleMessage(Message msg) {
            MainPresenterImpl presenter = refContext.get();
            switch (msg.what) {
                case IAccountManager.LOGIN_SUC:
                    //登录成功
                    presenter.view.showLoginSuc();
                    break;
                case IAccountManager.SERVER_FAIL:
                    //服务器错误
                    presenter.view.showError(IAccountManager.SERVER_FAIL, "");
                    break;
                case IAccountManager.TOKEN_INVALID:
                    //token 过期
                    presenter.view.showError(IAccountManager.SERVER_FAIL, "");
                    break;
            }
        }
    }

    public MainPresenterImpl(IMainView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;

        accountManager.setHandler(new MyHandler(this));
    }

    @Override
    public void loginByToken() {
        accountManager.loginByToken();
    }
}
