package com.languo.mooccar.account.model;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.languo.mooccar.MoocCarApplication;
import com.languo.mooccar.account.model.response.Account;
import com.languo.mooccar.account.model.response.LoginResponse;
import com.languo.mooccar.account.model.response.RegisterResponse;
import com.languo.mooccar.account.model.response.SmsCodeResponse;
import com.languo.mooccar.account.model.response.UserExistResponse;
import com.languo.mooccar.common.databus.RxBus;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.IRequest;
import com.languo.mooccar.common.http.IResponse;
import com.languo.mooccar.common.http.api.API;
import com.languo.mooccar.common.http.biz.BaseBizResponse;
import com.languo.mooccar.common.http.impl.BaseRequest;
import com.languo.mooccar.common.http.impl.BaseResponse;
import com.languo.mooccar.common.storage.SharedPreferencesDao;
import com.languo.mooccar.common.util.DevUtil;

import io.reactivex.functions.Function;

/**
 * Created by YuLiang on 2018/3/3.
 */

public class AccountManagerImpl implements IAccountManager {


    private IHttpClient httpClient;
    private SharedPreferencesDao preferencesDao;

    private static final String TAG = "AccountManagerImpl";

    public AccountManagerImpl(IHttpClient httpClient, SharedPreferencesDao preferencesDao) {
        this.httpClient = httpClient;
        this.preferencesDao = preferencesDao;
    }

    /**
     * 获取验证码
     *
     * @param phone
     */
    @Override
    public void fetchSMSCode(final String phone) {
        RxBus.getInstance().chainProcess(new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                IRequest request = new BaseRequest(API.Config.getDomain() + API.GET_SMS_CODE);
                request.setBody("phone", phone);
                IResponse baseResponse = httpClient.get(request, false);
                Log.i(TAG, "run: " + request.getUrl());
                Log.i(TAG, "run: " + baseResponse.getData());
                SmsCodeResponse bizResponse = new SmsCodeResponse();
                if (baseResponse.getCode() == BaseResponse.STATE_OK) {
                    bizResponse = new Gson().fromJson(baseResponse.getData(), SmsCodeResponse.class);
                    if (bizResponse.getCode() == BaseBizResponse.STATE_OK) {
                        //请求成功
                        bizResponse.setCode(SMS_SEND_SUC);
                    } else {
                        //请求失败
                        bizResponse.setCode(SMS_SEND_FAIL);
                    }
                } else {
                    //失败
                    bizResponse.setCode(SMS_SEND_FAIL);
                }
                return bizResponse;
            }
        });
    }

    /**
     * 校验验证码
     *
     * @param phone
     * @param smsCode
     */
    @Override
    public void checkSMSCode(final String phone, final String smsCode) {

        RxBus.getInstance().chainProcess(new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                String url = API.Config.getDomain() + API.CHECK_SMS_CODE;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("code", smsCode);
                IResponse response = httpClient.get(request, false);
                Log.d(TAG, response.getData());
                SmsCodeResponse smsCodeResponse = new SmsCodeResponse();
                if (response.getCode() == BaseResponse.STATE_OK) {
                    smsCodeResponse =
                            new Gson().fromJson(response.getData(), SmsCodeResponse.class);
                    if (smsCodeResponse.getCode() == BaseBizResponse.STATE_OK) {
                        smsCodeResponse.setCode(SMS_CHECK_SUC);
                    } else {
                        smsCodeResponse.setCode(SMS_CHECK_FAIL);
                    }
                } else {
                    smsCodeResponse.setCode(SMS_CHECK_FAIL);
                }
                return smsCodeResponse;
            }
        });
    }

    /**
     * 检查用户是否存在
     *
     * @param phone
     */
    @Override
    public void checkUserExist(final String phone) {
        RxBus.getInstance().chainProcess(new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                IRequest request = new BaseRequest(API.Config.getDomain() + API.CHECK_USER_EXIST);
                request.setBody("phone", phone);
                IResponse response = httpClient.get(request, false);
                Log.i(TAG, "run: " + response.getData());
                UserExistResponse userExistResponse = new UserExistResponse();
                if (response.getCode() == BaseResponse.STATE_OK) {
                    userExistResponse = new Gson().fromJson(response.getData(), UserExistResponse.class);
                    if (userExistResponse.getCode() == BaseBizResponse.STATE_USER_EXIST) {
                        userExistResponse.setCode(USER_EXIST);
                    } else if (userExistResponse.getCode() == BaseBizResponse.STATE_USER_NOT_EXIST) {
                        userExistResponse.setCode(USER_NOT_EXIST);
                    }
                } else {
                    userExistResponse.setCode(SERVER_FAIL);
                }
                return userExistResponse;
            }
        });
    }

    /**
     * 注册
     *
     * @param phone
     * @param password
     */
    @Override
    public void register(final String phone, final String password) {
        RxBus.getInstance().chainProcess(new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                String url = API.Config.getDomain() + API.REGISTER;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("password", password);
                request.setBody("uid", DevUtil.UUID(MoocCarApplication.getApplication()));

                IResponse response = httpClient.post(request, false);
                Log.i(TAG, "run: 注册" + response.getData());
                RegisterResponse bizResponse = new RegisterResponse();
                if (response.getCode() == BaseResponse.STATE_OK) {
                    bizResponse =
                            new Gson().fromJson(response.getData(), RegisterResponse.class);
                    if (bizResponse.getCode() == BaseBizResponse.STATE_OK) {
                        bizResponse.setCode(REGISTER_SUC);
                    } else {
                        bizResponse.setCode(SERVER_FAIL);
                    }
                } else {
                    bizResponse.setCode(SERVER_FAIL);
                }
                return bizResponse;
            }
        });

    }

    /**
     * 登录
     *
     * @param phone
     * @param password
     */
    @Override
    public void login(final String phone, final String password) {
        RxBus.getInstance().chainProcess(new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                String url = API.Config.getDomain() + API.LOGIN;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("password", password);

                IResponse response = httpClient.post(request, false);
                Log.i(TAG, "run: " + response.getData());

                LoginResponse loginResponse = new LoginResponse();
                if (response.getCode() == BaseBizResponse.STATE_OK) {
                    loginResponse = new Gson().fromJson(response.getData(), LoginResponse.class);
                    if (loginResponse.getCode() == BaseBizResponse.STATE_OK) {
                        //登录成功，保存数据
                        //TODO: 加密存储
                        SharedPreferencesDao sharedPreferencesDao = new SharedPreferencesDao(MoocCarApplication.getApplication(), SharedPreferencesDao.FILE_ACCOUNT);
                        sharedPreferencesDao.save(SharedPreferencesDao.KEY_ACCOUNT, loginResponse.getData());
                        //通知 UI
                        loginResponse.setCode(LOGIN_SUC);
                    } else if (loginResponse.getCode() == BaseBizResponse.STATE_PW_ERR) {
                        //密码错误
                        loginResponse.setCode(PW_ERROR);
                    } else {
                        //其他错误
                        loginResponse.setCode(SERVER_FAIL);
                    }
                } else {
                    //请求失败
                    loginResponse.setCode(SERVER_FAIL);
                }
                return loginResponse;
            }
        });
    }

    /**
     * 根据 token 登录
     */
    @Override
    public void loginByToken() {
        RxBus.getInstance().chainProcess(new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                //登录是否过期
                boolean tokenValid = false;
                Account account = (Account) preferencesDao.get(SharedPreferencesDao.KEY_ACCOUNT, Account.class);
                if (account != null) {
                    if (account.getExpired() > System.currentTimeMillis()) {
                        //token，没有过期
                        tokenValid = true;
                    }
                }
                LoginResponse loginResponse = new LoginResponse();
                if (!tokenValid) {
                    //token过期，显示输入手机号对话框
                    loginResponse.setCode(TOKEN_INVALID);
                    return loginResponse;
                }

                //自动登录，使用token
                IRequest request = new BaseRequest(API.Config.getDomain() + API.LOGIN_BY_TOKEN);
                request.setBody("token", account.getToken());
                IResponse response = httpClient.post(request, false);
                if (response.getCode() == BaseBizResponse.STATE_OK) {
                    //请求成功
                    loginResponse = new Gson().fromJson(response.getData(), LoginResponse.class);
                    if (loginResponse.getCode() == BaseBizResponse.STATE_OK) {
                        // 保存登录信息
                        account = loginResponse.getData();
                        // todo: 加密存储
                        preferencesDao.save(SharedPreferencesDao.KEY_ACCOUNT, account);
                        loginResponse.setCode(LOGIN_SUC);
                    } else if (loginResponse.getCode() == BaseBizResponse.STATE_TOKEN_INVALID) {
                        loginResponse.setCode(TOKEN_INVALID);
                    } else {
                        loginResponse.setCode(SERVER_FAIL);
                    }
                } else {
                    loginResponse.setCode(SERVER_FAIL);
                }
                return loginResponse;
            }
        });
    }
}
