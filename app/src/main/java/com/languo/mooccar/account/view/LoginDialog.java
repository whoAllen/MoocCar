package com.languo.mooccar.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.languo.mooccar.MoocCarApplication;
import com.languo.mooccar.R;
import com.languo.mooccar.account.model.AccountManagerImpl;
import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.model.response.LoginResponse;
import com.languo.mooccar.account.presenter.LoginDialogPresenterImpl;
import com.languo.mooccar.common.databus.RxBus;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.IRequest;
import com.languo.mooccar.common.http.IResponse;
import com.languo.mooccar.common.http.api.API;
import com.languo.mooccar.common.http.biz.BaseBizResponse;
import com.languo.mooccar.common.http.impl.BaseRequest;
import com.languo.mooccar.common.http.impl.OkHttpClientImpl;
import com.languo.mooccar.common.storage.SharedPreferencesDao;
import com.languo.mooccar.common.util.ToastUtil;

import java.lang.ref.SoftReference;


/**
 * Created by YuLiang on 2018/1/28.
 */

public class LoginDialog extends Dialog implements ILoginDialogView{

    private static final String TAG = "LoginDialog";
    private TextView mPhone;
    private EditText mPw;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private String phone;
    private LoginDialogPresenterImpl loginDialogPresenter;

    public LoginDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog, phone);
    }

    public LoginDialog(@NonNull Context context, int themeResId, String phone) {
        super(context, themeResId);
        this.phone = phone;
        IHttpClient httpClient = new OkHttpClientImpl();
        SharedPreferencesDao preferencesDao = new SharedPreferencesDao(
                MoocCarApplication.getApplication(),
                SharedPreferencesDao.FILE_ACCOUNT);
        AccountManagerImpl accountManager = new AccountManagerImpl(httpClient, preferencesDao);
        loginDialogPresenter = new LoginDialogPresenterImpl(this, accountManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_login_input, null);
        setContentView(rootView);
        initView();

        //注册 Presenter
        RxBus.getInstance().register(loginDialogPresenter);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mPhone = (TextView) findViewById(R.id.phone);
        mPw = (EditText) findViewById(R.id.password);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mLoading = findViewById(R.id.loading);
        mTips = (TextView) findViewById(R.id.tips);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //登录
                submit();
            }
        });
        mPhone.setText(phone);
    }

    /**
     * 登录
     */
    private void submit() {
        //显示 等待
        showOrHideLoading(true);
        loginDialogPresenter.requestLogin(phone, mPw.getText().toString());
    }

    @Override
    public void showLoading() {
        showOrHideLoading(true);
    }

    @Override
    public void showError(int code, String msg) {
        switch (code) {
            case IAccountManager.PW_ERROR:
                showOrHideLoading(false);
                mTips.setVisibility(View.VISIBLE);
                mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
                mTips.setText(getContext().getString(R.string.password_error));
                break;
            case IAccountManager.SERVER_FAIL:
                showOrHideLoading(false);
                mTips.setVisibility(View.VISIBLE);
                mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
                mTips.setText(getContext().getString(R.string.error_server));
                break;
        }
    }
    /**
     * 登录成功
     */
    @Override
    public void showLoginSuc() {
        mLoading.setVisibility(View.GONE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText(getContext().getString(R.string.login_suc));
        ToastUtil.show(getContext(), getContext().getString(R.string.login_suc));
        dismiss();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        //注销 RxBus
        RxBus.getInstance().unRegister(loginDialogPresenter);
    }

    /**
     * 显示进度条
     * @param show
     */
    private void showOrHideLoading(boolean show) {
        if(show) {
            //显示
            mLoading.setVisibility(View.VISIBLE);
            mBtnConfirm.setVisibility(View.GONE);
        } else {
            //隐藏
            mLoading.setVisibility(View.GONE);
            mBtnConfirm.setVisibility(View.VISIBLE);
        }
    }
}
