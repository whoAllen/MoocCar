package com.languo.mooccar.common.util;

import android.content.Context;
import android.widget.Toast;

import com.languo.mooccar.R;

/**
 * Created by YuLiang on 2018/1/28.
 *
 * Toast 工具类
 */

public class ToastUtil {
    public static void show(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }
}
