package com.languo.mooccar.account.presenter;


import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.languo.mooccar.R;
import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.view.ICreatePasswordDialogView;

import java.lang.ref.WeakReference;

/**
 * Created by YuLiang on 2018/3/3.
 */

public class CreatePasswordDialogPresenterImpl implements ICreatePasswordDialogPresenter {

    private IAccountManager accountManager;
    private ICreatePasswordDialogView view;

    static class MyHandler extends Handler {
        WeakReference<CreatePasswordDialogPresenterImpl> refContext;
        public MyHandler(CreatePasswordDialogPresenterImpl context) {
            refContext = new WeakReference(context);
        }

        @Override
        public void handleMessage(Message msg) {
            CreatePasswordDialogPresenterImpl presenter = refContext.get();
            super.handleMessage(msg);
            switch (msg.what) {
                case IAccountManager.LOGIN_SUC:
                    presenter.view.showLoginSuc();
                    break;
                case IAccountManager.REGISTER_SUC:
                    presenter.view.showRegisterSuc();
                    break;
                case IAccountManager.SERVER_FAIL:
                    presenter.view.showError(IAccountManager.SERVER_FAIL, "");
                    break;
                case IAccountManager.PW_ERROR:
                    presenter.view.showError(IAccountManager.PW_ERROR, "");
                    break;
            }
        }
    }

    public CreatePasswordDialogPresenterImpl(IAccountManager accountManager, ICreatePasswordDialogView view) {
        this.accountManager = accountManager;
        this.view = view;

        accountManager.setHandler(new MyHandler(this));
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
}
