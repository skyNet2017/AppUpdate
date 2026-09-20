package com.hss01248.update_zealot;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hss01248.appstartup.api.AppStartUpUtil;
import com.hss01248.appstartup.api.LogAppStartUpCallback;
import com.hss01248.update_default.UpdateAppDefault;
import com.vector.update_app.UpdateAppBean;
import com.vector.update_app.UpdateAppManager;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Zealot 检查更新 HttpManager：GET /api/apps/latest → UpdateAppBean。
 */
public class UpdateAppZealot extends UpdateAppDefault {

    private static final String TAG = "update-zealot";

    @Override
    public void asyncGet(@NonNull String url, @NonNull Map<String, String> params, @NonNull final Callback callBack) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        Log.i(TAG, message);
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        if (params != null && !params.isEmpty()) {
            if (!url.contains("?")) {
                url = url + "?";
            } else if (!url.endsWith("&") && !url.endsWith("?")) {
                url = url + "&";
            }
            for (String s : params.keySet()) {
                if (!url.contains(URLEncoder.encode(s) + "=")) {
                    url = url + URLEncoder.encode(s) + "=" + URLEncoder.encode(params.get(s)) + "&";
                }
            }
        }

        final String finalUrl = url;
        Log.i(TAG, "GET " + finalUrl);
        Request request = new Request.Builder()
                .get().url(finalUrl).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "request fail: " + finalUrl, e);
                callBack.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body() != null ? response.body().string() : "";
                Log.i(TAG, "HTTP " + response.code() + " bodyLen=" + json.length()
                        + " body=" + (json.length() > 800 ? json.substring(0, 800) + "..." : json));
                if (!response.isSuccessful()) {
                    callBack.onError(response.code() + "-" + response.message() + " " + json);
                    return;
                }
                try {
                    JSONObject root = new JSONObject(json);
                    if (root.has("error") && !root.isNull("error")) {
                        callBack.onError(root.optString("error"));
                        return;
                    }
                    ZealotUpdateInfo info = GsonUtils.fromJson(json, ZealotUpdateInfo.class);
                    UpdateAppBean bean = new UpdateAppBean();
                    copy(info, bean);
                    String out = GsonUtils.toJson(bean);
                    Log.i(TAG, "mapped bean: " + out);
                    callBack.onResponse(out);
                } catch (Exception e) {
                    Log.e(TAG, "json parse error", e);
                    callBack.onError("json parse error: \n" + e.getMessage() + "\n" + json);
                }
            }
        });
    }

    private void copy(ZealotUpdateInfo raw, UpdateAppBean bean) {
        ZealotUpdateInfo info = raw != null ? raw.effective() : null;
        if (info == null) {
            bean.setUpdate("No");
            Log.w(TAG, "no effective release in response");
            return;
        }

        int remoteCode = parseBuildVersion(info.build_version);
        bean.setVersionCode(remoteCode);
        bean.setNewVersion(info.release_version != null ? info.release_version : "");
        String apkUrl = resolveDirectApkUrl(info.install_url);
        bean.setApkFileUrl(apkUrl);
        bean.setUpdateLog(info.resolveChangelog());
        bean.setConstraint(false);
        if (info.size > 0) {
            bean.setTargetSize(ConvertUtils.byte2FitMemorySize(info.size, 1));
        }

        int localCode = AppUtils.getAppVersionCode();
        boolean hasNew = remoteCode > localCode;
        bean.setUpdate(hasNew ? "Yes" : "No");
        Log.i(TAG, "local=" + localCode + " remote=" + remoteCode
                + " version=" + info.release_version
                + " install_url=" + info.install_url
                + " apk_url=" + apkUrl
                + " hasNew=" + hasNew);
    }

    /**
     * 跟随 302，拿到真正的 .apk 直链，供应用内下载器使用（不跳浏览器）。
     */
    private static String resolveDirectApkUrl(String installUrl) {
        if (TextUtils.isEmpty(installUrl)) {
            return "";
        }
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();
            Request head = new Request.Builder().url(installUrl).head().build();
            Response resp = client.newCall(head).execute();
            try {
                String finalUrl = resp.request().url().toString();
                Log.i(TAG, "resolve apk url: " + installUrl + " -> " + finalUrl
                        + " http=" + resp.code()
                        + " type=" + resp.header("Content-Type"));
                if (!TextUtils.isEmpty(finalUrl)) {
                    return finalUrl;
                }
            } finally {
                resp.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "resolveDirectApkUrl fail, use install_url", e);
        }
        return installUrl;
    }

    private static int parseBuildVersion(String buildVersion) {
        if (TextUtils.isEmpty(buildVersion)) {
            return 0;
        }
        try {
            String s = buildVersion.trim();
            if (s.matches("\\d+")) {
                return Integer.parseInt(s);
            }
            int lastDash = s.lastIndexOf('-');
            if (lastDash >= 0 && s.substring(lastDash + 1).matches("\\d+")) {
                return Integer.parseInt(s.substring(lastDash + 1));
            }
            String digits = s.replaceAll("[^0-9]", "");
            if (TextUtils.isEmpty(digits)) {
                return 0;
            }
            return Integer.parseInt(digits);
        } catch (Exception e) {
            Log.w(TAG, "parse build_version fail: " + buildVersion, e);
            return 0;
        }
    }

    @NonNull
    @Override
    public String create(@NonNull Context context) {
        super.create(context);
        // 与蒲公英一致：应用内下载安装，不跳外部浏览器
        UpdateAppManager.setDownloadByBrowser(false);
        UpdateAppManager.setGuideToGooglePlay(false);
        AppStartUpUtil.add(new LogAppStartUpCallback() {
            @Override
            public void onFirstActivityCreated(Application app, Activity activity, Bundle savedInstanceState) {
                super.onFirstActivityCreated(app, activity, savedInstanceState);
                ThreadUtils.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "startup auto check update");
                        ZealotAppUpdateUtil.doUpdate();
                    }
                }, 3200);
            }
        });
        return "zealot updater";
    }
}
