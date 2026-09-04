package com.hss01248.update_default;


import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.vector.update_app.IGetParam;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.UpdateCallback;
import com.vector.update_app.listener.ExceptionHandler;

/**
 * @Despciption todo
 * @Author hss
 * @Date 28/07/2022 17:33
 * @Version 1.0
 */
public class AppUpdateUtil {

    private static final String BUILD_CONFIG_UPDATE_JSON_URL = "update_json_url";


    public static void config(IGetParam getParam){
        UpdateAppManager.setGetParam(getParam);
    }

    public static void setGuideToGooglePlay(boolean guideToGooglePlay) {
        UpdateAppManager.setGuideToGooglePlay(guideToGooglePlay);
    }

    public static void setDownloadByBrowser(boolean downloadByBrowser) {
        UpdateAppManager.setDownloadByBrowser(downloadByBrowser);
    }

    /**
     * 零配置检测更新：从 BuildConfig.update_json_url 读取七牛托管的 JSON 地址。
     * 需配合 uploadToQiniu.gradle 注入该字段。
     */
    public static void doUpdate() {
        doUpdate(true, null);
    }

    public static void doUpdate(boolean fromAppStart) {
        doUpdate(fromAppStart, null);
    }

    public static void doUpdate(boolean fromAppStart, @Nullable ExceptionHandler handler) {
        String url = readUpdateJsonUrlFromBuildConfig();
        if (TextUtils.isEmpty(url)) {
            LogUtils.w("没有找到 BuildConfig.update_json_url，无法发起更新请求。请 apply uploadToQiniu.gradle 并配置 qiniu_cdn_domain");
            if (handler != null) {
                handler.onException(new IllegalStateException("BuildConfig.update_json_url missing"));
            }
            return;
        }
        doUpdate(url, fromAppStart, handler);
    }

    public static void doUpdate(String url, boolean fromAppStart){
        doUpdate(url, fromAppStart, null);
    }

    public static void doUpdate(String url, boolean fromAppStart, @Nullable ExceptionHandler handler){
        new UpdateAppManager
                .Builder()
                .setFromAppStart(fromAppStart)
                .showLoadingAndToastError(!fromAppStart)
                .setUpdateUrl(url)
                .handleException(handler)
                .build()
                .update();
    }


    public static void doUpdate(String url,ExceptionHandler handler){
        doUpdate(url,true, handler);
    }

    public static void updateByClickBtn(){
        String url = readUpdateJsonUrlFromBuildConfig();
        if (TextUtils.isEmpty(url)) {
            LogUtils.w("没有找到 BuildConfig.update_json_url");
            return;
        }
        updateByClickBtn(url);
    }

    public static void updateByClickBtn(String url){
        new UpdateAppManager
                .Builder()
                .setUpdateUrl(url)
                .showLoadingAndToastError(true)
                .setActivity(ActivityUtils.getTopActivity())
                .setFromAppStart(false)
                .build()
                .update();
    }

    public static void checkUpdate(String url, UpdateCallback callback){
        new UpdateAppManager
                .Builder()
                .setUpdateUrl(url)
                .showLoadingAndToastError(false)
                .setFromAppStart(false)
                .build()
                .checkNewApp(callback);
    }

    @Nullable
    private static String readUpdateJsonUrlFromBuildConfig() {
        Class<?> reflect = null;
        try {
            reflect = Class.forName(AppUtils.getAppPackageName() + ".BuildConfig");
        } catch (ClassNotFoundException e) {
            LogUtils.i(e);
            String path = Utils.getApp().getClass().getName();
            LogUtils.i("application class path: " + path);
            int idx = path.lastIndexOf(".");
            if (idx > 0) {
                path = path.substring(0, idx);
                try {
                    reflect = Class.forName(path + ".BuildConfig");
                } catch (ClassNotFoundException ex) {
                    LogUtils.w(ex);
                }
            }
        }
        if (reflect == null) {
            return null;
        }
        try {
            Object value = reflect.getDeclaredField(BUILD_CONFIG_UPDATE_JSON_URL).get(reflect);
            if (value instanceof String) {
                String url = ((String) value).trim();
                LogUtils.i("get update_json_url: " + url);
                return url;
            }
        } catch (Throwable e) {
            LogUtils.i("app更新->没有配置 update_json_url", e);
        }
        return null;
    }
}
