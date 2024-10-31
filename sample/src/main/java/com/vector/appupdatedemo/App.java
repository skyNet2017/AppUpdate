package com.vector.appupdatedemo;

import android.app.Application;

import androidx.multidex.MultiDexApplication;

import com.hss01248.update_default.AppUpdateUtil;
import com.lzy.okgo.OkGo;
import com.zhy.http.okhttp.OkHttpUtils;

/**
 * Created by Vector
 * on 2017/7/17 0017.
 */

public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();


        OkHttpUtils.getInstance()
                .init(this)
                .debug(true, "okHttp")
                .timeout(20 * 1000);

        AppUpdateUtil.setGuideToGooglePlay(false);
        OkGo.getInstance().init(this);
    }
}
