package com.languo.mooccar.account.model;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.languo.mooccar.MoocCarApplication;
import com.languo.mooccar.account.model.response.Account;
import com.languo.mooccar.account.model.response.LoginResponse;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.IRequest;
import com.languo.mooccar.common.http.IResponse;
import com.languo.mooccar.common.http.api.API;
import com.languo.mooccar.common.http.biz.BaseBizResponse;
import com.languo.mooccar.common.http.impl.BaseRequest;
import com.languo.mooccar.common.http.impl.BaseResponse;
import com.languo.mooccar.common.storage.SharedPreferencesDao;
import com.languo.mooccar.common.util.DevUtil;

/**
 * Created by YuLiang on 2018/3/3.
 */

public class AccountManagerImpl implements IAccountManager {


    private IHttpClient httpClient;
    private SharedPreferencesDao preferencesDao;
    private Handler handler;

    private static final String TAG = "AccountManagerImpl";

    public AccountManagerImpl(IHttpClient httpClient, SharedPreferencesDao preferencesDao) {
        this.httpClient = httpClient;
        this.preferencesDao = preferencesDao;
    }


    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * 获取验证码
     * @param phone
     */
    @Override
    public void fetchSMSCode(final String phone) {
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
                        handler.sendEmptyMessage(SMS_SEND_SUC);
                    } else {
                        //请求失败
                        handler.sendEmptyMessage(SMS_SEND_FAIL);
                    }
                } else {
                    //失败
                    handler.sendEmptyMessage(SMS_SEND_FAIL);
                }
            }
        }).start();
    }

    /**
     * 校验验证码
     * @param phone
     * @param smsCode
     */
    @Override
    public void checkSMSCode(final String phone, final String smsCode) {
        new Thread() {
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.CHECK_SMS_CODE;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("code", smsCode);
                IResponse response = httpClient.get(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse bizRes =
                            new Gson().fromJson(response.getData(), BaseBizResponse.class);
                    if (bizRes.getCode() == BaseBizResponse.STATE_OK) {
                        handler.sendEmptyMessage(SMS_CHECK_SUC);
                    } else  {
                        handler.sendEmptyMessage(SMS_CHECK_FAIL);
                    }
                } else {
                    handler.sendEmptyMessage(SMS_CHECK_FAIL);
                }
            }
        }.start();
    }

    /**
     * 检查用户是否存在
     * @param phone
     */
    @Override
    public void checkUserExist(final String phone) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IRequest request = new BaseRequest(API.Config.getDomain() + API.CHECK_USER_EXIST);
                request.setBody("phone", phone);
                IResponse response = httpClient.get(request, false);
                Log.i(TAG, "run: " + response.getData());
                if(response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse baseBizResponse = new Gson().fromJson(response.getData(), BaseBizResponse.class);
                    if(baseBizResponse.getCode() == BaseBizResponse.STATE_USER_EXIST) {
                        handler.sendEmptyMessage(USER_EXIST);
                    } else if(baseBizResponse.getCode() == BaseBizResponse.STATE_USER_NOT_EXIST) {
                        handler.sendEmptyMessage(USER_NOT_EXIST);
                    }
                } else {
                    handler.sendEmptyMessage(SERVER_FAIL);
                }
            }
        }).start();
    }

    /**
     * 注册
     * @param phone
     * @param password
     */
    @Override
    public void register(final String phone, final String password) {
        new Thread() {
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.REGISTER;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("password", password);
                request.setBody("uid", DevUtil.UUID(MoocCarApplication.getApplication()));

                IResponse response = httpClient.post(request, false);
                Log.i(TAG, "run: 注册" + response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse bizRes =
                            new Gson().fromJson(response.getData(), BaseBizResponse.class);
                    if (bizRes.getCode() == BaseBizResponse.STATE_OK) {
                        handler.sendEmptyMessage(REGISTER_SUC);
                    } else {
                        handler.sendEmptyMessage(SERVER_FAIL);
                    }
                } else {
                    handler.sendEmptyMessage(SERVER_FAIL);
                }

            }
        }.start();
    }

    /**
     * 登录
     * @param phone
     * @param password
     */
    @Override
    public void login(final String phone, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.LOGIN;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
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
                        handler.sendEmptyMessage(LOGIN_SUC);
                    } else if(loginResponse.getCode() == BaseBizResponse.STATE_PW_ERR) {
                        //密码错误
                        handler.sendEmptyMessage(PW_ERROR);
                    } else {
                        //其他错误
                        handler.sendEmptyMessage(SERVER_FAIL);
                    }
                } else {
                    //请求失败
                    handler.sendEmptyMessage(SERVER_FAIL);
                }
            }
        }).start();
    }

    /**
     * 根据 token 登录
     */
    @Override
    public void loginByToken() {
        //登录是否过期
        boolean tokenValid = false;
        final Account account = (Account) preferencesDao.get(SharedPreferencesDao.KEY_ACCOUNT, Account.class);
        if(account != null) {
            if(account.getExpired() > System.currentTimeMillis()) {
                //token，没有过期
                tokenValid = true;
            }
        }
        if(!tokenValid) {
            //token过期，显示输入手机号对话框
            handler.sendEmptyMessage(TOKEN_INVALID);
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
                            // 保存登录信息
                            Account account = loginResponse.getData();
                            // todo: 加密存储
                            preferencesDao.save(SharedPreferencesDao.KEY_ACCOUNT, account);
                            handler.sendEmptyMessage(LOGIN_SUC);
                        } else if(loginResponse.getCode() == BaseBizResponse.STATE_TOKEN_INVALID) {
                            handler.sendEmptyMessage(TOKEN_INVALID);
                        } else {
                            handler.sendEmptyMessage(SERVER_FAIL);
                        }
                    } else {
                        handler.sendEmptyMessage(SERVER_FAIL);
                    }
                }
            }).start();
    }}
}
