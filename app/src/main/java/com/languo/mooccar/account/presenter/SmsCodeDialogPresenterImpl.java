package com.languo.mooccar.account.presenter;

import android.os.Handler;
import android.os.Message;

import com.languo.mooccar.account.model.AccountManagerImpl;
import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.view.ISmsCodeDialogView;
import com.languo.mooccar.account.view.IView;

import java.lang.ref.WeakReference;

/**
 * Created by YuLiang on 2018/3/3.
 */

public class SmsCodeDialogPresenterImpl implements ISmsCodeDialogPresenter {

    private ISmsCodeDialogView view;
    private IAccountManager iAccountManager;

    /**
     * 接收消息并处理
     */
    private static class MyHandler extends Handler{
        WeakReference<SmsCodeDialogPresenterImpl> refContext;
        public MyHandler(SmsCodeDialogPresenterImpl context) {
            refContext = new WeakReference(context);
        }

        @Override
        public void handleMessage(Message msg) {
            SmsCodeDialogPresenterImpl presenter = refContext.get();
            switch (msg.what) {
                case IAccountManager.SMS_SEND_SUC:
                    presenter.view.showCountDownTimer();
                    break;
                case IAccountManager.SMS_SEND_FAIL:
                    presenter.view.showError(IAccountManager.SMS_SEND_FAIL, "");
                    break;
                case IAccountManager.SMS_CHECK_SUC:
                    presenter.view.showSmsCodeCheckState(true);
                    break;
                case IAccountManager.SMS_CHECK_FAIL:
                    presenter.view.showSmsCodeCheckState(false);
                    break;
                case IAccountManager.USER_EXIST:
                    presenter.view.showUserExist(true);
                    break;
                case IAccountManager.USER_NOT_EXIST:
                    presenter.view.showUserExist(false);
                    break;
                case IAccountManager.SERVER_FAIL:
                    presenter.view.showError(IAccountManager.SERVER_FAIL, "");
                    break;
            }
        }
    }

    public SmsCodeDialogPresenterImpl(ISmsCodeDialogView view, IAccountManager iAccountManager) {
        this.view = view;
        this.iAccountManager = iAccountManager;
        iAccountManager.setHandler(new MyHandler(this));
    }

    @Override
    public void requestSendSmsCode(String phone) {
        iAccountManager.fetchSMSCode(phone);
    }

    @Override
    public void requestCheckSmsCode(String phone, String password) {
        iAccountManager.checkSMSCode(phone, password);
    }

    @Override
    public void requestCheckUserExist(String phone) {
        iAccountManager.checkUserExist(phone);
    }
}
