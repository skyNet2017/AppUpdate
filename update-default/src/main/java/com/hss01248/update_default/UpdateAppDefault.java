package com.hss01248.update_default;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.google.gson.Gson;

import com.hss01248.download_okhttp.IDownloadCallback;
import com.liulishuo.filedownloader2.AndroidDownloader;
import com.liulishuo.filedownloader2.DownloadCallbackOnMainThreadWrapper;
import com.vector.update_app.HttpManager;
import com.vector.update_app.UpdateAppManager;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
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
        callBack.onStart();
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
                    callBack.onError(response.code()+"\n"+response.message());
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
        callBack.onStart();
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
        AndroidDownloader.prepareDownload(url)
                        .filePath(file.getAbsolutePath())
                        .forceRedownload(false)
                        .start(new DownloadCallbackOnMainThreadWrapper(new IDownloadCallback() {
                            @Override
                            public void onCodeStart(String url, String path) {
                                IDownloadCallback.super.onCodeStart(url, path);
                                callback.onBefore();
                            }

                            @Override
                            public void onSuccess(String url, String path) {
                                callback.onResponse(new File(path));
                            }

                            @Override
                            public void onFailed(String url, String path, String code, String msg, Throwable e) {
                                callback.onError(code + " "+ msg);
                            }

                            @Override
                            public void onProgress(String url, String path, long total, long alreadyReceived, long speed) {
                                IDownloadCallback.super.onProgress(url, path, total, alreadyReceived, speed);
                                float progress = 0f;
                                if(total != 0){
                                    progress = alreadyReceived*1f/total;
                                }
                                callback.onProgress(progress,total);
                            }
                        }));
/*        FileDownloader.getImpl().create(url)
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
                .start();*/
    }

    @NonNull
    @Override
    public String create(@NonNull Context context) {
        UpdateAppManager.setDefaultHttpImpl(new UpdateAppDefault());
        return "updateBy all";
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}