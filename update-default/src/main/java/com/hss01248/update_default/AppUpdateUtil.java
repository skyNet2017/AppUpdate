package com.hss01248.update_default;


import com.blankj.utilcode.util.ActivityUtils;
import com.vector.update_app.IGetParam;
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


    public static void config(IGetParam getParam){
        UpdateAppManager.setGetParam(getParam);
    }

    public static void setGuideToGooglePlay(boolean guideToGooglePlay) {
        UpdateAppManager.setGuideToGooglePlay(guideToGooglePlay);
    }

    public static void setDownloadByBrowser(boolean downloadByBrowser) {
        UpdateAppManager.setDownloadByBrowser(downloadByBrowser);
    }

    public static void doUpdate(String url,boolean fromAppStart){
        new UpdateAppManager
                .Builder()
                .setFromAppStart(fromAppStart)
                .showLoadingAndToastError(!fromAppStart)
                //当前Activity
                // .setActivity(this)
                //更新地址
                .setUpdateUrl(url)
                .handleException(null)
                //实现httpManager接口的对象
                //.setHttpManager(new UpdateHttpImpl())
                .build()
                .update();
    }


    public static void doUpdate(String url,ExceptionHandler handler){
        doUpdate(url,true);
    }

    public static void updateByClickBtn(String url){
        new UpdateAppManager
                .Builder()
                //当前Activity
                // .setActivity(this)
                //更新地址
                .setUpdateUrl(url)
                .showLoadingAndToastError(true)
                .setActivity(ActivityUtils.getTopActivity())
                .setFromAppStart(false)
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
                .showLoadingAndToastError(false)
                .setFromAppStart(false)
                //.showLoadingAndToastError(true)
                //.handleException(new )
                //实现httpManager接口的对象
                //.setHttpManager(new UpdateHttpImpl())
                .build()
                .checkNewApp(callback);
    }
}
