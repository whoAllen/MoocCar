package com.languo.mooccar.account.presenter;


import com.languo.mooccar.account.model.AccountManagerImpl;
import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.model.response.SmsCodeResponse;
import com.languo.mooccar.account.model.response.UserExistResponse;
import com.languo.mooccar.account.view.ISmsCodeDialogView;
import com.languo.mooccar.common.databus.RegisterBus;

/**
 * Created by YuLiang on 2018/3/3.
 */

public class SmsCodeDialogPresenterImpl implements ISmsCodeDialogPresenter {

    private ISmsCodeDialogView view;
    private IAccountManager iAccountManager;

    public SmsCodeDialogPresenterImpl(ISmsCodeDialogView view, IAccountManager iAccountManager) {
        this.view = view;
        this.iAccountManager = iAccountManager;
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

    @RegisterBus
    public void onSmsCodeResponse(SmsCodeResponse smsCodeResponse) {
        switch (smsCodeResponse.getCode()) {
            case AccountManagerImpl.SMS_SEND_SUC:
                view.showCountDownTimer();
                break;
            case AccountManagerImpl.SMS_SEND_FAIL:
                view.showError(IAccountManager.SMS_SEND_FAIL, "");
                break;
            case AccountManagerImpl.SMS_CHECK_SUC:
                view.showSmsCodeCheckState(true);
                break;
            case AccountManagerImpl.SMS_CHECK_FAIL:
                view.showSmsCodeCheckState(false);
                break;
        }
    }

    @RegisterBus
    public void onUserExistResponse(UserExistResponse userExistResponse) {
        switch (userExistResponse.getCode()) {
            case AccountManagerImpl.USER_EXIST:
                view.showUserExist(true);
                break;
            case AccountManagerImpl.USER_NOT_EXIST:
                view.showUserExist(false);
                break;
            case AccountManagerImpl.SERVER_FAIL:
                view.showError(IAccountManager.SERVER_FAIL, "");
                break;
        }
    }
}
