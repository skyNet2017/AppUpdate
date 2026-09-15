package com.hss01248.update_pyger;


import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
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
            Class reflect = null;
            try {
                 reflect = Class.forName(AppUtils.getAppPackageName() + ".BuildConfig");

            } catch (ClassNotFoundException e) {
                LogUtils.i(e);
                String path = Utils.getApp().getClass().getName();
                LogUtils.i("application class path: "+path);
                path = path.substring(0,path.lastIndexOf("."));
                try {
                    reflect =   Class.forName(path + ".BuildConfig");
                } catch (ClassNotFoundException ex) {
                    LogUtils.w(ex);
                }
            }
            if(reflect == null){
                LogUtils.w("没有找到BuildConfig,拿不到蒲公英的key,无法发起更新请求");
                return;
            }
            try {
                apiKey = (String) reflect.getDeclaredField("pyger_api_key").get(reflect);
                appKey = (String) reflect.getDeclaredField("pyger_app_key").get(reflect);
                LogUtils.i("get api_key and app_key : "+ apiKey+"->"+appKey);
            } catch (Throwable e) {
                LogUtils.i("app更新->没有配置相关key",e);
                //return;
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
