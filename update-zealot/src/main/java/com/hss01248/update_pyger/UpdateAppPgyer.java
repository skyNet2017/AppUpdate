package com.hss01248.update_pyger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

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
 * @Despciption todo
 * @Author hss
 * @Date 30/01/2023 09:41
 * @Version 1.0
 */
public class UpdateAppPgyer extends UpdateAppDefault {


    @Override
    public void asyncGet(@NonNull String url, @NonNull Map<String, String> params, @NonNull final Callback callBack) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        if(params!= null && !params.isEmpty()){
            if(!url.contains("?")){
                url = url+"?";
            }else {
                if(!url.endsWith("&")){
                    url = url+"&";
                }
            }
            for (String s : params.keySet()) {
                if(!url.contains(URLEncoder.encode(s)+"=")){
                    url = url + URLEncoder.encode(s)+"="+URLEncoder.encode(params.get(s))+"&";
                }
            }
        }

        Request request = new Request.Builder()
                .get().url(url).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onError( e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body() !=  null){

                    String json = response.body().string();
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(json);
                        int code = jsonObject.optInt("code", -1);
                        if(code != 0){
                            callBack.onError(code+"-"+jsonObject.optString("message"));
                            Log.w("update",code+"-"+jsonObject.optString("message"));
                            return;
                        }
                        String dataJson = jsonObject.optString("data");
                        PgyerUpdateInfo info = GsonUtils.fromJson(dataJson,PgyerUpdateInfo.class);
                        UpdateAppBean bean = new UpdateAppBean();
                        copy(info,bean);
                        String json2 = GsonUtils.toJson(bean);
                        callBack.onResponse(json2);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callBack.onError("json parse error: \n"+e.getMessage()+"\n"+json);
                    }

                }else {
                    callBack.onError(response.code()+"-"+response.message());
                }
            }
        });
    }

    private void copy(PgyerUpdateInfo info, UpdateAppBean bean) {
        bean.setVersionCode(Integer.parseInt(info.buildVersionNo));
        bean.setConstraint(info.needForceUpdate);
        bean.setNewVersion(info.buildVersion);
        //https://cos.pgyer.com/c4ea46b542c0bfc55c6b97e454fc5200.apk?sign=f190b2ce783e605ae23d2113758fa681&t=1675045195
        // &response-content-disposition=attachment%3Bfilename%3DFinalCompress_2.1.8-debug.apk

        //需要签名算法算出sign
        //或者直接用webview加载downloadURl,在webview内下载


        //自动会重定向到真正的下载链接
        bean.setApkFileUrl(info.downloadURL);
        bean.setUpdate("Yes");
        bean.setUpdateLog(TextUtils.isEmpty(info.buildUpdateDescription) ? info.buildDescription: info.buildUpdateDescription);
        bean.setTargetSize(ConvertUtils.byte2FitMemorySize(Long.parseLong(info.buildFileSize),1));
        //bean.setNewMd5()
    }

    @Override
    public void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull FileCallback callback) {
        super.download(url, path, fileName, callback);
        //使用webview加载url并下载,或者使用外部浏览器下载apk
       /* Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ActivityUtils.getTopActivity().startActivity(intent);
        callback.onError("跳到外部浏览器去下载");*/
    }


    @NonNull
    @Override
    public String create(@NonNull Context context) {
        super.create(context);
        AppStartUpUtil.add(new LogAppStartUpCallback(){
            @Override
            public void onFirstActivityCreated(Application app, Activity activity, Bundle savedInstanceState) {
                super.onFirstActivityCreated(app, activity, savedInstanceState);
                ThreadUtils.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PygerAppUpdateUtil.doUpdate();
                    }
                },1200);
            }
        });
        return "pyger updater";
    }
}
