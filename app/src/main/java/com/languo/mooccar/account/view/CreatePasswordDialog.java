package com.languo.mooccar.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.languo.mooccar.MoocCarApplication;
import com.languo.mooccar.R;
import com.languo.mooccar.account.model.AccountManagerImpl;
import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.presenter.CreatePasswordDialogPresenterImpl;
import com.languo.mooccar.common.databus.RxBus;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.impl.OkHttpClientImpl;
import com.languo.mooccar.common.storage.SharedPreferencesDao;
import com.languo.mooccar.common.util.ToastUtil;

/**
 * Created by YuLiang on 2018/1/20.
 * 密码创建/修改 对话框
 */

public class CreatePasswordDialog extends Dialog implements ICreatePasswordDialogView{

    private View mRoot;
    private String phone;

    private TextView mTitle;
    private TextView mPhone;
    private EditText mPw;
    private EditText mPw1;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;

    private CreatePasswordDialogPresenterImpl presenter;

    public CreatePasswordDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog, phone);

    }

    public CreatePasswordDialog(@NonNull Context context, int themeResId, String phone) {
        super(context, themeResId);
        this.phone = phone;
        IHttpClient httpClient = new OkHttpClientImpl();
        SharedPreferencesDao preferencesDao =
                new SharedPreferencesDao(MoocCarApplication.getApplication()
                , SharedPreferencesDao.FILE_ACCOUNT);
        IAccountManager accountManager = new AccountManagerImpl(httpClient, preferencesDao);
        presenter = new CreatePasswordDialogPresenterImpl(accountManager, this);

        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_pw, null);
        setContentView(mRoot);

        //注册 Presenter
        RxBus.getInstance().register(presenter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initListener();
    }

    /**
     * 初始化点击事件
     */
    private void initListener() {
        mPhone = (TextView) findViewById(R.id.phone);
        mPw = (EditText) findViewById(R.id.pw);
        mPw1 = (EditText) findViewById(R.id.pw1);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mLoading = findViewById(R.id.loading);
        mTips = (TextView) findViewById(R.id.tips);
        mTitle = (TextView) findViewById(R.id.dialog_title);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        mPhone.setText(phone);
    }

    /**
     * 确定，提交注册
     */
    private void submit() {
        String password = mPw.getText().toString().trim();
        String password1 = mPw1.getText().toString();
        if(presenter.checkPw(password, password1)) {
            final String phonePhone = phone;
            // 请求网络， 提交注册
            presenter.register(phonePhone, password);
        }
    }

    /**
     * 注册成功
     */
    @Override
    public void showRegisterSuc() {
        mLoading.setVisibility(View.VISIBLE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext()
                .getResources()
                .getColor(R.color.color_text_normal));
        mTips.setText(getContext()
                .getString(R.string.register_suc_and_loging));
        presenter.login(phone, mPw.getText().toString());
    }

    /**
     * UI 显示 登录成功
     */
    @Override
    public void showLoginSuc() {
        dismiss();
        ToastUtil.show(getContext(), getContext().getString(R.string.login_suc));
    }

    @Override
    public void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(int code, String msg) {
        switch (code) {
            case IAccountManager.PW_ERROR:
                ToastUtil.show(getContext(),
                        getContext().getString(R.string.error_server));
                break;
            case IAccountManager.SERVER_FAIL:
                mTips.setTextColor(getContext()
                        .getResources().getColor(R.color.error_red));
                mTips.setText(getContext().getString(R.string.error_server));
                break;
        }
    }

    @Override
    public void showPasswordNull() {
        mTips.setVisibility(View.VISIBLE);
        mTips.setText(getContext().getString(R.string.password_is_null));
        mTips.setTextColor(getContext().
                getResources().getColor(R.color.error_red));
    }

    @Override
    public void showPasswordNotEqual() {
        mTips.setVisibility(View.VISIBLE);
        mTips.setText(getContext()
                .getString(R.string.password_is_not_equal));
        mTips.setTextColor(getContext()
                .getResources().getColor(R.color.error_red));
    }
}
