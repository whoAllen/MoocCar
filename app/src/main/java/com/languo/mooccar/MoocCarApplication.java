package com.languo.mooccar;

import android.app.Application;

/**
 * Created by YuLiang on 2018/1/28.
 */

public class MoocCarApplication extends Application {

    private static MoocCarApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static MoocCarApplication getApplication() {
        return application;
    }
}
