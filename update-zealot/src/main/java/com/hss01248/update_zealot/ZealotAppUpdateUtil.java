package com.hss01248.update_zealot;

import android.text.TextUtils;
import android.util.Log;

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
 * Zealot 6.2.x 的 /api/apps/latest 若带 release_version/build_version 会服务端 500，
 * 因此只传 channel_key，在客户端比较 versionCode。
 */
public class ZealotAppUpdateUtil {

    private static final String TAG = "update-zealot";
    public static final String DEFAULT_ENDPOINT = "https://appstore.timefly.art";

    public static void doUpdate() {
        doUpdate(null, null, new ExceptionHandler() {
            @Override
            public void onException(Exception e) {
                Log.e(TAG, "doUpdate exception", e);
                LogUtils.w(e);
            }
        });
    }

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
            Log.w(TAG, "未配置 zealot_channel_key，无法发起更新请求");
            LogUtils.w("app更新->未配置 zealot_channel_key，无法发起更新请求");
            return;
        }

        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        String url = base + "/api/apps/latest?channel_key=" + enc(channelKey);

        Log.i(TAG, "check update local=" + AppUtils.getAppVersionName()
                + "(" + AppUtils.getAppVersionCode() + ") url=" + url);
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

    private static String[] readBuildConfig() {
        String endpoint = null;
        String channelKey = null;
        Class<?> reflect = null;
        try {
            reflect = Class.forName(AppUtils.getAppPackageName() + ".BuildConfig");
        } catch (ClassNotFoundException e) {
            LogUtils.i(e);
            String path = Utils.getApp().getClass().getName();
            path = path.substring(0, path.lastIndexOf("."));
            try {
                reflect = Class.forName(path + ".BuildConfig");
            } catch (ClassNotFoundException ex) {
                LogUtils.w(ex);
            }
        }
        if (reflect == null) {
            Log.w(TAG, "没有找到 BuildConfig");
            return new String[]{null, null};
        }
        try {
            endpoint = (String) reflect.getDeclaredField("zealot_endpoint").get(null);
            channelKey = (String) reflect.getDeclaredField("zealot_channel_key").get(null);
            Log.i(TAG, "BuildConfig endpoint=" + endpoint + " channelKey="
                    + (channelKey == null ? "null" : channelKey.substring(0, Math.min(8, channelKey.length())) + "..."));
        } catch (Throwable e) {
            Log.e(TAG, "读 BuildConfig 失败", e);
        }
        return new String[]{endpoint, channelKey};
    }
}
