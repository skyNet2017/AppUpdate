package com.hss01248.appupdate.http;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.hss01248.http.ConfigInfo;
import com.hss01248.http.HttpUtil;
import com.hss01248.http.callback.MyNetCallback;
import com.hss01248.http.config.FileDownlodConfig;
import com.hss01248.http.response.ResponseBean;
import com.vector.update_app.HttpManager;
import com.vector.update_app.UpdateAppManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by Vector
 * on 2017/6/19 0019.
 */

public class UpdateAppByHttpUtil implements HttpManager, Initializer<String> {
    /**
     * 异步get
     *
     * @param url      get请求地址
     * @param params   get参数
     * @param callBack 回调
     */
    @Override
    public void asyncGet(@NonNull String url, @NonNull Map<String, String> params, @NonNull final Callback callBack) {
        HttpUtil.requestString(url)
                .get()
                .addParams2(params)
                .callback(new MyNetCallback<ResponseBean<String>>() {
                    @Override
                    public void onSuccess(ResponseBean<String> response) {
                        callBack.onResponse(response.data);
                    }

                    @Override
                    public void onError(String msgCanShow) {
                        callBack.onError(msgCanShow);
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
        HttpUtil.requestString(url)
                .post()
                .addParams2(params)
                .callback(new MyNetCallback<ResponseBean<String>>() {
                    @Override
                    public void onSuccess(ResponseBean<String> response) {
                        callBack.onResponse(response.data);
                    }

                    @Override
                    public void onError(String msgCanShow) {
                        callBack.onError(msgCanShow);
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
        HttpUtil.download(url)
                .setFileDownlodConfig(FileDownlodConfig.newBuilder().fileDir(path).fileName(fileName).build())
                .callback(new MyNetCallback<ResponseBean<FileDownlodConfig>>() {
                    @Override
                    public void onProgressChange(long transPortedBytes, long totalBytes, ConfigInfo info) {
                        super.onProgressChange(transPortedBytes, totalBytes, info);
                        float progress = 0f;
                        if(totalBytes != 0){
                            progress = transPortedBytes*1f/totalBytes;
                        }
                        callback.onProgress(progress, totalBytes);
                    }

                    @Override
                    public void onSuccess(ResponseBean<FileDownlodConfig> response) {
                        callback.onResponse(new File(path,fileName));
                    }

                    @Override
                    public void onError(String msgCanShow) {
                        callback.onError(msgCanShow);
                    }

                });
        callback.onBefore();
    }

    @NonNull
    @Override
    public String create(@NonNull Context context) {
        UpdateAppManager.setDefaultHttpImpl(new UpdateAppByHttpUtil());
        return "updateBy all";
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}