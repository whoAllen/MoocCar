package com.languo.mooccar.account;

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
import com.languo.mooccar.account.response.LoginResponse;
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

public class LoginDialog extends Dialog {

    private static final String TAG = "LoginDialog";
    private static final int LOGIN_SUC = 1;
    private static final int SERVER_FAIL = 2;
    private static final int PW_ERR = 4;
    private TextView mPhone;
    private EditText mPw;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private String phone;
    private IHttpClient httpClient;
    private MyHandler myHandler;

    static class MyHandler extends Handler{
        static SoftReference<LoginDialog> softReference;
        public MyHandler(LoginDialog loginDialog) {
            softReference = new SoftReference<LoginDialog>(loginDialog);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginDialog loginDialog = softReference.get();
            if(loginDialog == null) {
                return;
            }

            switch (msg.what) {
                case LOGIN_SUC:
                    loginDialog.showLoginSuc();
                    break;
                case  SERVER_FAIL:
                    loginDialog.showServerError();
                    break;
                case PW_ERR:
                    loginDialog.showPasswordError();
                    break;
            }
        }
    }

    /**
     * 密码错误
     */
    private void showPasswordError() {
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.password_error));
    }

    /**
     * 登录出错
     */
    private void showServerError() {
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }



    /**
     * 登录成功
     */
    private void showLoginSuc() {
        mLoading.setVisibility(View.GONE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText(getContext().getString(R.string.login_suc));
        ToastUtil.show(getContext(), getContext().getString(R.string.login_suc));
        dismiss();
    }

    public LoginDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog, phone);
    }

    public LoginDialog(@NonNull Context context, int themeResId, String phone) {
        super(context, themeResId);
        this.phone = phone;

        httpClient = new OkHttpClientImpl();
        myHandler = new MyHandler(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_login_input, null);
        setContentView(rootView);
        initView();
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.LOGIN;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                String password = mPw.getText().toString();
                request.setBody("password", password);

                IResponse response = httpClient.post(request, false);
                Log.i(TAG, "run: " + response.getData());
                if(response.getCode() == BaseBizResponse.STATE_OK) {
                    LoginResponse loginResponse = new Gson().fromJson(response.getData(), LoginResponse.class);
                    if(loginResponse.getCode() == BaseBizResponse.STATE_OK) {
                        //登录成功，保存数据
                        //TODO: 加密存储
                        SharedPreferencesDao sharedPreferencesDao = new SharedPreferencesDao(MoocCarApplication.getApplication(), SharedPreferencesDao.FILE_ACCOUNT);
                        sharedPreferencesDao.save(SharedPreferencesDao.KEY_ACCOUNT, loginResponse.getData());

                        //通知 UI
                        myHandler.sendEmptyMessage(LOGIN_SUC);
                    } else if(loginResponse.getCode() == BaseBizResponse.STATE_PW_ERR) {
                        //密码错误
                        myHandler.sendEmptyMessage(PW_ERR);
                    } else {
                        //其他错误
                        myHandler.sendEmptyMessage(SERVER_FAIL);
                    }
                } else {
                    //请求失败
                    myHandler.sendEmptyMessage(SERVER_FAIL);
                }
            }
        }).start();
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
