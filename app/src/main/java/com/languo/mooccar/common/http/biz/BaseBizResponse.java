package com.languo.mooccar.common.http.biz;

/**
 * Created by YuLiang on 2018/1/20.
 *
 * 返回业务数据的公共格式
 *
 */

public class BaseBizResponse {

    public static final int STATE_OK = 200;
    // 用户已经存在
    public static int STATE_USER_EXIST = 100003;
    // 用户不存在
    public static int STATE_USER_NOT_EXIST = 100002;
    //密码错误
    public static int STATE_PW_ERR = 100005;
    //token过期
    public static int STATE_TOKEN_INVALID = 100006;

    /**
     * code : 200
     * msg : code has send
     */

    private int code;
    private String msg;

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
