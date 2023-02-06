package com.hss01248.update_default;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;


import com.blankj.utilcode.util.ThreadUtils;
import com.google.gson.Gson;
import com.hss01248.appstartup.api.AppStartUpUtil;
import com.hss01248.appstartup.api.LogAppStartUpCallback;
import com.hss01248.update_default.pgyer.PygerAppUpdateUtil;
import com.hss01248.update_default.pgyer.UpdateAppPgyer;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.vector.update_app.HttpManager;
import com.vector.update_app.UpdateAppManager;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


/**
 * Created by Vector
 * on 2017/6/19 0019.
 */

public class UpdateAppDefault implements HttpManager, Initializer<String> {
    /**
     * 异步get
     *
     * @param url      get请求地址
     * @param params   get参数
     * @param callBack 回调
     */
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
                url = url + URLEncoder.encode(s)+"="+URLEncoder.encode(params.get(s))+"&";
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
                    callBack.onResponse(response.body().string());
                }else {
                    callBack.onError(response.code()+"-"+response.message());
                }
            }
        });
    }

    /**
     * 异步post
     *
     * @param url      post请求地址
     * @param params   post请求参数
     * @param callBack 回调
     */
    @Override
    public void asyncPost(@NonNull String url, @NonNull Map<String, String> params, @NonNull final Callback callBack) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        RequestBody body  = null;
        if(!params.isEmpty()){
            String json = new Gson().toJson(params);
            body = RequestBody.create(MediaType.parse("application/json"),json);
        }
        Request request = new Request.Builder()
                .post(body)
                .url(url)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onError( e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful() && response.body() !=  null){
                    callBack.onResponse(response.body().string());
                }else {
                    callBack.onError(response.code()+"-"+response.message());
                }
            }
        });

    }

    /**
     * 下载
     *
     * @param url      下载地址
     * @param path     文件保存路径
     * @param fileName 文件名称
     * @param callback 回调
     */
    @Override
    public void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull final FileCallback callback) {
        File file = new File(path,fileName);
        FileDownloader.getImpl().create(url)
                .setPath(file.getAbsolutePath())
                //1M以上,需要wifi才下载
                .setWifiRequired(false)
                .setCallbackProgressTimes(300)
                .setMinIntervalUpdateSpeed(400)
                .setForceReDownload(false)
                .setListener(new FileDownloadSampleListener(){
                    @Override
                    protected void started(BaseDownloadTask task) {
                        super.started(task);
                        callback.onBefore();
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.progress(task, soFarBytes, totalBytes);
                        float progress = 0f;
                        if(totalBytes != 0){
                            progress = soFarBytes*1f/totalBytes;
                        }
                        callback.onProgress(progress,totalBytes);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        super.completed(task);
                        callback.onResponse(file);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        super.error(task, e);
                        callback.onError(e.getMessage());
                    }
                })
                .start();
    }

    @NonNull
    @Override
    public String create(@NonNull Context context) {
        UpdateAppManager.setDefaultHttpImpl(new UpdateAppPgyer());
        FileDownloader.setup(context);
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
        return "updateBy all";
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}