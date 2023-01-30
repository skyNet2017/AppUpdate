package com.hss01248.update_default.pgyer;


import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.update_default.UpdateAppDefault;
import com.liulishuo.filedownloader.FileDownloader;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.listener.ExceptionHandler;

/**
 * @Despciption todo
 * @Author hss
 * @Date 28/07/2022 17:33
 * @Version 1.0
 */
public class PygerAppUpdateUtil {



    public static void doUpdate(String apiKey,String apiToken,ExceptionHandler handler){
        FileDownloader.setup(Utils.getApp());
        UpdateAppManager.setDefaultHttpImpl(new UpdateAppPgyer());
        String url = "https://www.pgyer.com/apiv2/app/check?_api_key="+apiKey+"&token="+apiToken+"&buildVersion="+ AppUtils.getAppVersionCode();
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
}
