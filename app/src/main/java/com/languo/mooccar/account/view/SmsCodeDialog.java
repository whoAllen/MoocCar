package com.languo.mooccar.account.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dalimao.corelibrary.VerificationCodeInput;
import com.google.gson.Gson;
import com.languo.mooccar.MoocCarApplication;
import com.languo.mooccar.R;
import com.languo.mooccar.account.model.AccountManagerImpl;
import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.presenter.ISmsCodeDialogPresenter;
import com.languo.mooccar.account.presenter.SmsCodeDialogPresenterImpl;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.IRequest;
import com.languo.mooccar.common.http.IResponse;
import com.languo.mooccar.common.http.api.API;
import com.languo.mooccar.common.http.biz.BaseBizResponse;
import com.languo.mooccar.common.http.impl.BaseRequest;
import com.languo.mooccar.common.http.impl.BaseResponse;
import com.languo.mooccar.common.http.impl.OkHttpClientImpl;
import com.languo.mooccar.common.storage.SharedPreferencesDao;
import com.languo.mooccar.common.util.ToastUtil;

import java.lang.ref.SoftReference;

/**
 * Created by YuLiang on 2018/1/20.
 */

public class SmsCodeDialog extends Dialog implements ISmsCodeDialogView{

    private View mRoot;
    private String phone;
    private Button btReSend;
    private TextView tvPhone;
    private VerificationCodeInput dialogVerificationCodeInput;
    private ProgressBar progressBar;
    private TextView tvError;

    private SmsCodeDialogPresenterImpl presenter;

    /**
     * 倒计时类
     */
    private CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
        @SuppressLint("StringFormatMatches")
        @Override
        public void onTick(long l) {
            btReSend.setEnabled(false);
            btReSend.setText(String.format(getContext().getString(R.string.after_time_resend, l/1000)));
        }

        @Override
        public void onFinish() {
            btReSend.setEnabled(true);
            btReSend.setText(getContext().getString(R.string.resend));
            cancel();
        }
    };

    public SmsCodeDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog, phone);
    }

    public SmsCodeDialog(@NonNull Context context, int themeResId, String phone) {
        super(context, themeResId);
        this.phone = phone;

        IHttpClient httpClient = new OkHttpClientImpl();
        SharedPreferencesDao preferencesDao = new SharedPreferencesDao(
                MoocCarApplication.getApplication(),
                SharedPreferencesDao.FILE_ACCOUNT);
        AccountManagerImpl accountManager = new AccountManagerImpl(httpClient, preferencesDao);
        presenter = new SmsCodeDialogPresenterImpl(this, accountManager);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sms_code, null);
        setContentView(mRoot);
        tvPhone = (TextView) mRoot.findViewById(R.id.dialog_phone);
        tvError = (TextView) mRoot.findViewById(R.id.error);
        btReSend = (Button) mRoot.findViewById(R.id.btn_resend);
        dialogVerificationCodeInput = (VerificationCodeInput) mRoot.findViewById(R.id.dialog_verificationCodeInput);
        progressBar = (ProgressBar) mRoot.findViewById(R.id.loading);

        //设置文字，验证码已经下发
        String codeIsSend = getContext().getString(R.string.sending);
        tvPhone.setText(String.format(codeIsSend, phone));
        initListener();
        requestCode();
    }

    /**
     * 请求下发验证码
     */
    private void requestCode() {
        presenter.requestSendSmsCode(phone);
    }


    private void initListener() {
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btReSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCode();
                String template = getContext().getString(R.string.sending);
                tvPhone.setText(String.format(template, phone));
            }
        });

        //监听验证码的输入，输入完成后进行验证
        dialogVerificationCodeInput.setOnCompleteListener(new VerificationCodeInput.Listener() {
            @Override
            public void onComplete(String s) {
                //输入完成，发送验证
                commit(s);
            }
        });

    }

    /**
     * 向服务端验证 验证码
     */
    private void commit(final String code) {
        presenter.requestCheckSmsCode(phone, code);
    }

    /**
     * 显示进度
     */
    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(int code, String msg) {
        progressBar.setVisibility(View.GONE);
        switch (code) {
            case IAccountManager.SMS_SEND_FAIL:
                ToastUtil.show(getContext(), getContext().getString(R.string.sms_send_fail));
                break;
            case IAccountManager.SMS_CHECK_FAIL:
                //提示验证码错误
                tvError.setVisibility(View.VISIBLE);
                dialogVerificationCodeInput.setEnabled(true);
                break;
            case IAccountManager.SERVER_FAIL:
                ToastUtil.show(getContext(), getContext().getString(R.string.error_server));
                break;
        }
    }

    @Override
    public void showCountDownTimer() {
        tvPhone.setText(String.format(getContext()
                .getString(R.string.sms_code_send_phone), phone));
        countDownTimer.start();
        btReSend.setEnabled(false);
    }

    @Override
    public void showSmsCodeCheckState(boolean b) {
        if (!b) {
            //提示验证码错误
            tvError.setVisibility(View.VISIBLE);
            dialogVerificationCodeInput.setEnabled(true);
            progressBar.setVisibility(View.GONE);
        } else {

            tvError.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            presenter.requestCheckUserExist(phone);
        }
    }

    /**
     * 显示用户是否存在的反应
     */
    @Override
    public void showUserExist(boolean exist) {
        dismiss();
        if(exist) {
            //用户存在 登录
            LoginDialog loginDialog = new LoginDialog(getContext(), phone);
            loginDialog.show();
        } else {
            //用户不存在 注册
            CreatePasswordDialog createPasswordDialog = new CreatePasswordDialog(getContext(), phone);
            createPasswordDialog.show();
        }
    }
}
