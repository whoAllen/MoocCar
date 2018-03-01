package com.languo.mooccar.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.languo.mooccar.MoocCarApplication;
import com.languo.mooccar.R;
import com.languo.mooccar.account.view.PhoneInputDialog;
import com.languo.mooccar.account.model.response.Account;
import com.languo.mooccar.account.model.response.LoginResponse;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.IRequest;
import com.languo.mooccar.common.http.IResponse;
import com.languo.mooccar.common.http.api.API;
import com.languo.mooccar.common.http.biz.BaseBizResponse;
import com.languo.mooccar.common.http.impl.BaseRequest;
import com.languo.mooccar.common.http.impl.OkHttpClientImpl;
import com.languo.mooccar.common.storage.SharedPreferencesDao;
import com.languo.mooccar.common.util.ToastUtil;

/**
 * 1、检查本地状态
 * 2、若用户没登录则登录
 * 3、登录之前校验验证码
 * 4、token 有效，使用 token 自动登录
 *
 * TODO:地图初始化
 */
public class MainActivity extends AppCompatActivity {

    private IHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        httpClient = new OkHttpClientImpl();
        checkLoginState();
    }

    /**
     * 检查用户登录状态
     */
    private void checkLoginState() {
        //登录是否过期
        boolean tokenValid = false;
        //获取本地保存的登录信息
        SharedPreferencesDao sharedPreferencesDao =
                new SharedPreferencesDao(MoocCarApplication.getApplication(), SharedPreferencesDao.FILE_ACCOUNT);
        final Account account = (Account) sharedPreferencesDao.get(SharedPreferencesDao.KEY_ACCOUNT, Account.class);
        if(account != null) {
            if(account.getExpired() > System.currentTimeMillis()) {
                //token，没有过期
                tokenValid = true;
            }
        }
        if(!tokenValid) {
            //token过期，显示输入手机号对话框
            showPhoneInputDialog();
        } else {
            //自动登录，使用token
            new Thread(new Runnable() {
                @Override
                public void run() {
                    IRequest request = new BaseRequest(API.Config.getDomain() + API.LOGIN_BY_TOKEN);
                    request.setBody("token", account.getToken());
                    IResponse response = httpClient.post(request, false);
                    if(response.getCode() == BaseBizResponse.STATE_OK) {
                        //请求成功
                        LoginResponse loginResponse = new Gson().fromJson(response.getData(), LoginResponse.class);
                        if(loginResponse.getCode() == BaseBizResponse.STATE_OK) {
                            //登录成功,更新数据
                            SharedPreferencesDao sharedPreferencesDao1 =
                                    new SharedPreferencesDao(MoocCarApplication.getApplication(), SharedPreferencesDao.FILE_ACCOUNT);
                            sharedPreferencesDao1.save(SharedPreferencesDao.KEY_ACCOUNT, loginResponse.getData());
                            //通知 UI
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.show(MainActivity.this, getString(R.string.login_suc));
                                }
                            });
                        } else if(loginResponse.getCode() == BaseBizResponse.STATE_TOKEN_INVALID) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showPhoneInputDialog();
                                }
                            });
                        } else {
                            //未知错误
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.show(MainActivity.this,
                                            getString(R.string.error_server));
                                }
                            });
                        }
                    } else {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(MainActivity.this,
                                        getString(R.string.error_server));
                            }
                        });
                    }
                }
            }).start();
        }
    }

    /**
     * 显示输入手机号的对话框
     */
    private void showPhoneInputDialog() {
        PhoneInputDialog phoneInputDialog = new PhoneInputDialog(MainActivity.this);
        phoneInputDialog.show();
    }
}
