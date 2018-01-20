package com.languo.mooccar.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.languo.mooccar.R;
import com.languo.mooccar.account.PhoneInputDialog;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.impl.OkHttpClientImpl;

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
        //todo:获取本地保存的登录信息

        //登录是否过期
        boolean tokenValid = false;

        if(!tokenValid) {
            //token过期，显示输入手机号对话框
            showPhoneInputDialog();

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
