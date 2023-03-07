package com.hss01248.update_default;


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



    public static void doUpdate(String url,ExceptionHandler handler){
        new UpdateAppManager
                .Builder()
                //当前Activity
                // .setActivity(this)
                //更新地址
                .setUpdateUrl(url)
                 .handleException(handler)
                //实现httpManager接口的对象
                //.setHttpManager(new UpdateHttpImpl())
                .build()
                .update();
    }

    public static void updateByClickBtn(String url){
        new UpdateAppManager
                .Builder()
                //当前Activity
                // .setActivity(this)
                //更新地址
                .setUpdateUrl(url)
                .showLoadingAndToastError(true)
                //.handleException(new )
                //实现httpManager接口的对象
                //.setHttpManager(new UpdateHttpImpl())
                .build()
                .update();
    }

    public static void checkUpdate(String url, UpdateCallback callback){
        new UpdateAppManager
                .Builder()
                //当前Activity
                // .setActivity(this)
                //更新地址
                .setUpdateUrl(url)
                //.showLoadingAndToastError(true)
                //.handleException(new )
                //实现httpManager接口的对象
                //.setHttpManager(new UpdateHttpImpl())
                .build()
                .checkNewApp(callback);
    }
}
