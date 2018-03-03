package com.languo.mooccar.main.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.languo.mooccar.MoocCarApplication;
import com.languo.mooccar.R;
import com.languo.mooccar.account.model.AccountManagerImpl;
import com.languo.mooccar.account.model.IAccountManager;
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
import com.languo.mooccar.main.presenter.MainPresenterImpl;

/**
 * 1、检查本地状态
 * 2、若用户没登录则登录
 * 3、登录之前校验验证码
 * 4、token 有效，使用 token 自动登录
 *
 * TODO:地图初始化
 */
public class MainActivity extends AppCompatActivity implements IMainView{

    private MainPresenterImpl mainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IHttpClient httpClient = new OkHttpClientImpl();
        SharedPreferencesDao preferencesDao =
                new SharedPreferencesDao(MoocCarApplication.getApplication(),
                        SharedPreferencesDao.FILE_ACCOUNT);
        IAccountManager accountManager = new AccountManagerImpl(httpClient, preferencesDao);
        mainPresenter = new MainPresenterImpl(this, accountManager);
//        checkLoginState();
        mainPresenter.loginByToken();
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
            mainPresenter.loginByToken();
        }
    }

    /**
     * 显示输入手机号的对话框
     */
    private void showPhoneInputDialog() {
        PhoneInputDialog phoneInputDialog = new PhoneInputDialog(MainActivity.this);
        phoneInputDialog.show();
    }


    @Override
    public void showLoading() {

    }

    @Override
    public void showError(int code, String msg) {
        switch (code) {
            case IAccountManager.SERVER_FAIL:
                showPhoneInputDialog();
                break;
            case IAccountManager.TOKEN_INVALID:
                ToastUtil.show(this, getString(R.string.token_invalid));
                showPhoneInputDialog();
                break;
        }
    }

    @Override
    public void showLoginSuc() {
        ToastUtil.show(this, getString(R.string.login_suc));
    }
}
