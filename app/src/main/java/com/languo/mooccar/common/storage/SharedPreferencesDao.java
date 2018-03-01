package com.languo.mooccar.common.storage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by YuLiang on 2018/1/28.
 */

public class SharedPreferencesDao {
    public static final String FILE_ACCOUNT = "FILE_ACCOUNT";//SharePreference 存储用户信息时的文件名
    public static final String KEY_ACCOUNT = "KEY_ACCOUNT";//SharePreference 存储用户信息对象时的Key
    private SharedPreferences sharedPreferences;
    private static final String TAG = "SharedPreferencesDao";

    public SharedPreferencesDao(Application application, String fileName) {
        sharedPreferences = application.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    /**
     * 保存 key-value 到 SharePreference文件
     * @param key
     * @param value
     */
    public void save(String key, String value) {
        sharedPreferences.edit().putString(key, value).commit();
    }

    /**
     * 读取 k - v
     * @param key
     * @return
     */
    public String get(String key) {
        return sharedPreferences.getString(key, null);
    }

    /**
     * 保存对象
     * @param key
     * @param object
     */
    public void save(String key, Object object) {
        String value = new Gson().toJson(object);
        save(key, value);
    }

    /**
     * 读取对象
     * @param key
     * @param cls
     * @return
     */
    public Object get(String key, Class cls) {
        String value = get(key);
        try {
            if(value != null) {
                Object o = new Gson().fromJson(value, cls);
                return o;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}
