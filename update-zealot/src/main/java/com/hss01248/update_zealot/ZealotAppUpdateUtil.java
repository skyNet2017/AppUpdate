package com.hss01248.update_zealot;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.listener.ExceptionHandler;

import java.net.URLEncoder;

/**
 * Zealot App 内检查更新入口。
 * <p>
 * 配置来自宿主 BuildConfig：{@code zealot_endpoint}、{@code zealot_channel_key}
 * （由 uploadToZealot.gradle 注入）。token 不进 APK。
 */
public class ZealotAppUpdateUtil {

    public static final String DEFAULT_ENDPOINT = "https://appstore.timefly.art";

    public static void doUpdate() {
        doUpdate(null, null, new ExceptionHandler() {
            @Override
            public void onException(Exception e) {
                LogUtils.w(e);
            }
        });
    }

    /**
     * @param endpoint   如 https://appstore.timefly.art；空则读 BuildConfig / 默认
     * @param channelKey 渠道 key（不是 slug）；空则读 BuildConfig
     */
    public static void doUpdate(@Nullable String endpoint, @Nullable String channelKey,
                                ExceptionHandler handler) {
        String[] cfg = readBuildConfig();
        if (TextUtils.isEmpty(endpoint)) {
            endpoint = cfg[0];
        }
        if (TextUtils.isEmpty(channelKey)) {
            channelKey = cfg[1];
        }
        if (TextUtils.isEmpty(endpoint)) {
            endpoint = DEFAULT_ENDPOINT;
        }
        if (TextUtils.isEmpty(channelKey)) {
            LogUtils.w("app更新->未配置 zealot_channel_key，无法发起更新请求");
            return;
        }

        String versionName = AppUtils.getAppVersionName();
        int versionCode = AppUtils.getAppVersionCode();
        if (TextUtils.isEmpty(versionName)) {
            versionName = "0";
        }

        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        String url = base + "/api/apps/latest"
                + "?channel_key=" + enc(channelKey)
                + "&release_version=" + enc(versionName)
                + "&build_version=" + enc(String.valueOf(versionCode));

        LogUtils.i("zealot check update: " + url);
        new UpdateAppManager
                .Builder()
                .setUpdateUrl(url)
                .handleException(handler)
                .setHttpManager(new UpdateAppZealot())
                .build()
                .update();
    }

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s == null ? "" : s, "UTF-8");
        } catch (Exception e) {
            return s == null ? "" : s;
        }
    }

    /** @return [endpoint, channelKey] */
    private static String[] readBuildConfig() {
        String endpoint = null;
        String channelKey = null;
        Class<?> reflect = null;
        try {
            reflect = Class.forName(AppUtils.getAppPackageName() + ".BuildConfig");
        } catch (ClassNotFoundException e) {
            LogUtils.i(e);
            String path = Utils.getApp().getClass().getName();
            LogUtils.i("application class path: " + path);
            path = path.substring(0, path.lastIndexOf("."));
            try {
                reflect = Class.forName(path + ".BuildConfig");
            } catch (ClassNotFoundException ex) {
                LogUtils.w(ex);
            }
        }
        if (reflect == null) {
            LogUtils.w("没有找到 BuildConfig，拿不到 Zealot 配置");
            return new String[]{null, null};
        }
        try {
            endpoint = (String) reflect.getDeclaredField("zealot_endpoint").get(null);
            channelKey = (String) reflect.getDeclaredField("zealot_channel_key").get(null);
            LogUtils.i("get zealot_endpoint/channel_key: " + endpoint + " -> " + channelKey);
        } catch (Throwable e) {
            LogUtils.i("app更新->没有配置 Zealot BuildConfig 字段", e);
        }
        return new String[]{endpoint, channelKey};
    }
}
