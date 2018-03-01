package com.languo.mooccar.account.model.response;

import com.languo.mooccar.common.http.biz.BaseBizResponse;

/**
 * Created by YuLiang on 2018/1/28.
 */

public class LoginResponse extends BaseBizResponse{
    Account data;

    public Account getData() {
        return data;
    }

    public void setData(Account data) {
        this.data = data;
    }
}
