package com.hss01248.update_default.pgyer;


import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ReflectUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.update_default.UpdateAppDefault;
import com.liulishuo.filedownloader.FileDownloader;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.listener.ExceptionHandler;

import java.lang.reflect.Field;

/**
 * @Despciption todo
 * @Author hss
 * @Date 28/07/2022 17:33
 * @Version 1.0
 */
public class PygerAppUpdateUtil {

    public static void doUpdate(){
        doUpdate("", "", new ExceptionHandler() {
            @Override
            public void onException(Exception e) {
                LogUtils.w(e);
            }
        });
    }

    public static void doUpdate(@Nullable String apiKey,@Nullable  String apiToken, ExceptionHandler handler){
        if(TextUtils.isEmpty(apiKey) || TextUtils.isEmpty(apiToken)){

            try {
                Class reflect = Class.forName(AppUtils.getAppPackageName() + ".BuildConfig");
               apiKey = (String) reflect.getDeclaredField("pyger_api_key").get(reflect);
                apiToken = (String) reflect.getDeclaredField("pyger_api_token").get(reflect);
                LogUtils.i("get key and token : "+ apiKey+"->"+apiToken);
            } catch (Exception e) {
                LogUtils.w(e);
            }
        }
        FileDownloader.setup(Utils.getApp());
        UpdateAppManager.setDefaultHttpImpl(new UpdateAppPgyer());
        String url = "https://www.pgyer.com/apiv2/app/check?_api_key="+apiKey+"&token="+apiToken+"&buildVersion="+ AppUtils.getAppVersionCode();
        new UpdateAppManager
                .Builder()
                //当前Activity
                // .setActivity(this)
                //更新地址
                .setUpdateUrl(url)
                 .handleException(handler)
                //实现httpManager接口的对象
                //.setHttpManager(new UpdateHttpImpl())
                .build()
                .update();
    }
}
