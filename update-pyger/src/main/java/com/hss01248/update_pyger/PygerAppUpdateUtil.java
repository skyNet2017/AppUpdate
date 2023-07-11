package com.hss01248.update_pyger;


import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.liulishuo.filedownloader.FileDownloader;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.listener.ExceptionHandler;

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

    /**
     * https://www.pgyer.com/doc/view/api#appUpdate
     * @param apiKey
     * @param appKey
     * @param handler
     */
    public static void doUpdate(@Nullable String apiKey,@Nullable  String appKey, ExceptionHandler handler){
        if(TextUtils.isEmpty(apiKey) || TextUtils.isEmpty(appKey)){

            try {
                Class reflect = Class.forName(AppUtils.getAppPackageName() + ".BuildConfig");
               apiKey = (String) reflect.getDeclaredField("pyger_api_key").get(reflect);
                appKey = (String) reflect.getDeclaredField("pyger_app_key").get(reflect);
                LogUtils.i("get api_key and app_key : "+ apiKey+"->"+appKey);
            } catch (Exception e) {
                LogUtils.w(e);
            }
        }
        //FileDownloader.setup(Utils.getApp());
        //UpdateAppManager.setDefaultHttpImpl(new UpdateAppPgyer());
        String url = "https://www.pgyer.com/apiv2/app/check?_api_key="+apiKey+"&appKey="+appKey+"&buildVersion="+ AppUtils.getAppVersionCode();
        new UpdateAppManager
                .Builder()
                //当前Activity
                // .setActivity(this)
                //更新地址
                .setUpdateUrl(url)
                 .handleException(handler)
                .setHttpManager(new UpdateAppPgyer())
                //实现httpManager接口的对象
                //.setHttpManager(new UpdateHttpImpl())
                .build()
                .update();
    }
}
