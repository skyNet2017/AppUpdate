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

import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

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
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
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

        Request request = new Request.Builder()
                .get().url(url).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body() != null ? response.body().string() : "";
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
                    callBack.onResponse(GsonUtils.toJson(bean));
                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.onError("json parse error: \n" + e.getMessage() + "\n" + json);
                }
            }
        });
    }

    private void copy(ZealotUpdateInfo raw, UpdateAppBean bean) {
        ZealotUpdateInfo info = raw != null ? raw.effective() : null;
        if (info == null) {
            bean.setUpdate("No");
            return;
        }

        int remoteCode = parseBuildVersion(info.build_version);
        bean.setVersionCode(remoteCode);
        bean.setNewVersion(info.release_version != null ? info.release_version : "");
        bean.setApkFileUrl(info.install_url != null ? info.install_url : "");
        bean.setUpdateLog(info.resolveChangelog());
        bean.setConstraint(false);
        if (info.size > 0) {
            bean.setTargetSize(ConvertUtils.byte2FitMemorySize(info.size, 1));
        }

        int localCode = AppUtils.getAppVersionCode();
        boolean hasNew = remoteCode > localCode;
        bean.setUpdate(hasNew ? "Yes" : "No");
        Log.i(TAG, "local=" + localCode + " remote=" + remoteCode
                + " version=" + info.release_version + " hasNew=" + hasNew);
    }

    private static int parseBuildVersion(String buildVersion) {
        if (TextUtils.isEmpty(buildVersion)) {
            return 0;
        }
        try {
            // 兼容 "1002" 或 "1.0.02-1002" 末尾数字
            String s = buildVersion.trim();
            if (s.matches("\\d+")) {
                return Integer.parseInt(s);
            }
            int lastDash = s.lastIndexOf('-');
            if (lastDash >= 0 && s.substring(lastDash + 1).matches("\\d+")) {
                return Integer.parseInt(s.substring(lastDash + 1));
            }
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            Log.w(TAG, "parse build_version fail: " + buildVersion, e);
            return 0;
        }
    }

    @NonNull
    @Override
    public String create(@NonNull Context context) {
        super.create(context);
        AppStartUpUtil.add(new LogAppStartUpCallback() {
            @Override
            public void onFirstActivityCreated(Application app, Activity activity, Bundle savedInstanceState) {
                super.onFirstActivityCreated(app, activity, savedInstanceState);
                ThreadUtils.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ZealotAppUpdateUtil.doUpdate();
                    }
                }, 1200);
            }
        });
        return "zealot updater";
    }
}
