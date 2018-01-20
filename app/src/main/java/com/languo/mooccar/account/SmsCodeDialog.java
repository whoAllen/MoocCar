package com.languo.mooccar.account;

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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dalimao.corelibrary.VerificationCodeInput;
import com.google.gson.Gson;
import com.languo.mooccar.R;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.IRequest;
import com.languo.mooccar.common.http.IResponse;
import com.languo.mooccar.common.http.api.API;
import com.languo.mooccar.common.http.biz.BaseBizResponse;
import com.languo.mooccar.common.http.impl.BaseRequest;
import com.languo.mooccar.common.http.impl.BaseResponse;
import com.languo.mooccar.common.http.impl.OkHttpClientImpl;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Created by YuLiang on 2018/1/20.
 */

public class SmsCodeDialog extends Dialog {

    private View mRoot;
    private String phone;
    private Button btReSend;
    private TextView tvPhone;
    private VerificationCodeInput dialogVerificationCodeInput;
    private ProgressBar progressBar;
    private TextView tvError;

    private IHttpClient httpClient;
    private MyHandler myHandler;

    private static final String TAG = "SmsCodeDialog";
    private static final int SMS_SEND_SUC = 1;
    private static final int SMS_SEND_FAIL = -1;
    private static final int SMS_CHECK_SUC = 2;
    private static final int SMS_CHECK_FAIL = -2;

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

    /**
     *  Handler 类，修改 UI
     */
    static class MyHandler extends Handler {
        SoftReference<SmsCodeDialog> smsCodeDialogSoftReference;
        public MyHandler(SmsCodeDialog smsCodeDialog) {
            smsCodeDialogSoftReference = new SoftReference<SmsCodeDialog>(smsCodeDialog);
        }

        @Override
        public void handleMessage(Message msg) {
            SmsCodeDialog smsCodeDialog = smsCodeDialogSoftReference.get();
            if(smsCodeDialog == null) {
                return;
            }
            //处理 UI 变化
            switch (msg.what) {
                case SMS_SEND_SUC:
                    smsCodeDialog.countDownTimer.start();
                    break;
                case SMS_SEND_FAIL:
                    Toast.makeText(smsCodeDialog.getContext(), R.string.sms_send_fail, Toast.LENGTH_SHORT).show();
                    break;
                case SMS_CHECK_SUC:
                    smsCodeDialog.showVerifyState(true);
                    break;
                case SMS_CHECK_FAIL:
                    smsCodeDialog.showVerifyState(false);
                    break;
            }
        }
    }

    public SmsCodeDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog, phone);
    }

    public SmsCodeDialog(@NonNull Context context, int themeResId, String phone) {
        super(context, themeResId);
        this.phone = phone;
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

        httpClient = new OkHttpClientImpl();
        myHandler = new MyHandler(this);

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                IRequest request = new BaseRequest(API.Config.getDomain() + API.GET_SMS_CODE);
                request.setBody("phone", phone);
                IResponse baseResponse =  httpClient.get(request, false);
                Log.i(TAG, "run: " + request.getUrl());
                Log.i(TAG, "run: " + baseResponse.getData());

                if(baseResponse.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse bizResponse = new Gson().fromJson(baseResponse.getData(), BaseBizResponse.class);
                    if(bizResponse.getCode() == BaseBizResponse.STATE_OK) {
                        //请求成功
                        myHandler.sendEmptyMessage(SMS_SEND_SUC);
                    } else {
                        //请求失败
                        myHandler.sendEmptyMessage(SMS_SEND_FAIL);
                    }
                } else {
                    //失败
                    myHandler.sendEmptyMessage(SMS_SEND_FAIL);
                }
            }
        }).start();

    }


    private void initListener() {
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
        showLoading();

        new Thread() {
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.CHECK_SMS_CODE;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("code", code);
                IResponse response = httpClient.get(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse bizRes =
                            new Gson().fromJson(response.getData(), BaseBizResponse.class);
                    if (bizRes.getCode() == BaseBizResponse.STATE_OK) {
                        myHandler.sendEmptyMessage(SMS_CHECK_SUC);
                    } else  {
                        myHandler.sendEmptyMessage(SMS_CHECK_FAIL);
                    }
                } else {
                    myHandler.sendEmptyMessage(SMS_CHECK_FAIL);
                }

            }
        }.start();
    }

    /**
     * 显示进度
     */
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 对应的验证码验证结果
     * @param suc
     */
    private void showVerifyState(boolean suc) {
        if(suc) {
            //验证成功
            progressBar.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
        } else {
            //验证失败
            progressBar.setVisibility(View.GONE);
            tvError.setVisibility(View.VISIBLE);
            dialogVerificationCodeInput.setEnabled(true);
        }
    }
}
