package com.languo.mooccar.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.languo.mooccar.MoocCarApplication;
import com.languo.mooccar.R;
import com.languo.mooccar.account.response.Account;
import com.languo.mooccar.account.response.LoginResponse;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.IRequest;
import com.languo.mooccar.common.http.IResponse;
import com.languo.mooccar.common.http.api.API;
import com.languo.mooccar.common.http.biz.BaseBizResponse;
import com.languo.mooccar.common.http.impl.BaseRequest;
import com.languo.mooccar.common.http.impl.BaseResponse;
import com.languo.mooccar.common.http.impl.OkHttpClientImpl;
import com.languo.mooccar.common.storage.SharedPreferencesDao;
import com.languo.mooccar.common.util.DevUtil;
import com.languo.mooccar.common.util.ToastUtil;

import java.lang.ref.SoftReference;

/**
 * Created by YuLiang on 2018/1/20.
 * 密码创建/修改 对话框
 */

public class CreatePasswordDialog extends Dialog{

    private View mRoot;
    private String phone;

    private TextView mTitle;
    private TextView mPhone;
    private EditText mPw;
    private EditText mPw1;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;

    private IHttpClient mHttpClient;
    private MyHandler mHandler;

    private  static final String TAG = "CreatePasswordDialog";
    private static final int REGISTER_SUC = 1;
    private static final int SERVER_FAIL = 100;
    private static final int LOGIN_SUC = 2;

    static class MyHandler extends Handler{
        SoftReference<CreatePasswordDialog> softReference;

        public MyHandler(CreatePasswordDialog createPasswordDialog) {
            softReference = new SoftReference<CreatePasswordDialog>(createPasswordDialog);
        }

        @Override
        public void handleMessage(Message msg) {
            CreatePasswordDialog createPasswordDialog = softReference.get();
            if(createPasswordDialog == null) {
                return;
            }
            switch (msg.what) {
                case REGISTER_SUC:
                    createPasswordDialog.showRegisterSuc();
                    break;
                case LOGIN_SUC:
                    createPasswordDialog.showLoginSuc();
                    break;
                case SERVER_FAIL:
                    createPasswordDialog.showServerError();
                    break;
            }
        }
    }


    public CreatePasswordDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog, phone);

    }

    public CreatePasswordDialog(@NonNull Context context, int themeResId, String phone) {
        super(context, themeResId);
        this.phone = phone;

        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_pw, null);
        setContentView(mRoot);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new MyHandler(this);
        mHttpClient = new OkHttpClientImpl();
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
        if(checkPassword()) {
            final String password = mPw.getText().toString();
            final String phonePhone = phone;
            // 请求网络， 提交注册
            new Thread() {
                @Override
                public void run() {
                    String url = API.Config.getDomain() + API.REGISTER;
                    IRequest request = new BaseRequest(url);
                    request.setBody("phone", phonePhone);
                    request.setBody("password", password);
                    request.setBody("uid", DevUtil.UUID(getContext()));

                    IResponse response = mHttpClient.post(request, false);
                    Log.i(TAG, "run: 注册" + response.getData());
                    if (response.getCode() == BaseResponse.STATE_OK) {
                        BaseBizResponse bizRes =
                                new Gson().fromJson(response.getData(), BaseBizResponse.class);
                        if (bizRes.getCode() == BaseBizResponse.STATE_OK) {
                            mHandler.sendEmptyMessage(REGISTER_SUC);
                        } else {
                            mHandler.sendEmptyMessage(SERVER_FAIL);
                        }
                    } else {
                        mHandler.sendEmptyMessage(SERVER_FAIL);
                    }

                }
            }.start();
        }
    }

    /**
     * 检查用户输入是否正确
     * @return
     */
    private boolean checkPassword() {
        String password = mPw.getText().toString().trim();
        if(TextUtils.isEmpty(password)) {
            //密码为空，提示
            mTips.setVisibility(View.VISIBLE);
            mTips.setText(getContext().getString(R.string.password_is_null));
            mTips.setTextColor(getContext().
                    getResources().getColor(R.color.error_red));
            return false;
        }
        if(!password.equals(mPw1.getText().toString().trim())) {
            mTips.setVisibility(View.VISIBLE);
            mTips.setText(getContext()
                    .getString(R.string.password_is_not_equal));
            mTips.setTextColor(getContext()
                    .getResources().getColor(R.color.error_red));
            return false;
        }
        return true;
    }

    /**
     * 注册错误
     */
    private void showServerError() {
        mTips.setTextColor(getContext()
                .getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }

    /**
     * 注册成功
     */
    private void showRegisterSuc() {
        mLoading.setVisibility(View.VISIBLE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext()
                .getResources()
                .getColor(R.color.color_text_normal));
        mTips.setText(getContext()
                .getString(R.string.register_suc_and_loging));

        // 请求网络，完成自动登录
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.LOGIN;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                String password = mPw.getText().toString();
                request.setBody("password", password);

                IResponse response = mHttpClient.post(request, false);
                Log.i(TAG, "run: 登录" + response.getData());
                if(response.getCode() == BaseBizResponse.STATE_OK) {
                    LoginResponse loginResponse = new Gson().fromJson(response.getData(), LoginResponse.class);
                    if(loginResponse.getCode() == BaseBizResponse.STATE_OK) {
                        //登录成功，保存登录信息
                        Account account = loginResponse.getData();
                        SharedPreferencesDao sharedPreferencesDao = new SharedPreferencesDao(MoocCarApplication.getApplication(),
                                SharedPreferencesDao.FILE_ACCOUNT);
                        sharedPreferencesDao.save(SharedPreferencesDao.KEY_ACCOUNT, account);
                        //通知 UI
                        mHandler.sendEmptyMessage(LOGIN_SUC);
                    } else {
                        //登录失败
                        mHandler.sendEmptyMessage(SERVER_FAIL);
                    }
                } else {
                        //登录失败
                    mHandler.sendEmptyMessage(SERVER_FAIL);
                }
            }
        }).start();

    }

    /**
     * UI 显示 登录成功
     */
    public void showLoginSuc() {
        dismiss();
        ToastUtil.show(getContext(), getContext().getString(R.string.login_suc));
    }
}
