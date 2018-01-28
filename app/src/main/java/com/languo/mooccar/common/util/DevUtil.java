package com.languo.mooccar.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by YuLiang on 2018/1/20.
 *
 * 获取设备信息工具类
 *
 */

public class DevUtil {

    /**
     * 获取 UID
     * @param context
     * @return
     */
    public static String UUID(Context context) {
        TelephonyManager tm = (TelephonyManager)context
                .getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String deviceId = tm.getDeviceId();
        return deviceId + System.currentTimeMillis();
    }

}
