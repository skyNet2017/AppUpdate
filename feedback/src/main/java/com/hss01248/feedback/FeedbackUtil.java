package com.hss01248.feedback;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ReflectUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hss01248.activityresult.StartActivityUtil;
import com.hss01248.activityresult.TheActivityListener;
import com.hss01248.basewebview.BaseQuickWebview;
import com.hss01248.basewebview.BaseWebviewActivity;
import com.hss01248.toast.MyToast;

/**
 * @Despciption todo
 * @Author hss
 * @Date 30/01/2023 11:12
 * @Version 1.0
 */
public class FeedbackUtil {


    /**
     * 需要预先在蒲公英应用设置页面打开应用反馈功能
     * @param url
     */
    public static void showPygerFeedback(String url){

        StartActivityUtil.startActivity(ActivityUtils.getTopActivity(), BaseWebviewActivity.class,null,false,
                new TheActivityListener<BaseWebviewActivity>(){

                    @Override
                    protected void onActivityCreated(@NonNull BaseWebviewActivity activity, @Nullable Bundle savedInstanceState) {
                        super.onActivityCreated(activity, savedInstanceState);
                       activity.getIntent().putExtra("url",url);
                        ThreadUtils.getMainHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    BaseQuickWebview quickWebview = ReflectUtils.reflect(activity)
                                            .field("quickWebview").get();
                                    WebView mainWebView = quickWebview.getWebView();
                                   float height =  mainWebView.getContentHeight() * mainWebView.getScale();//得到的是网页在手机上真实的高度

                                    float scroll=   mainWebView.getContentHeight() * mainWebView.getScale()-mainWebView.getHeight();//减去webview控件的高度得到的是网页上下可滚动的范围

                                    quickWebview.getWebView().scrollTo(0, (int) scroll - SizeUtils.dp2px(50));
                                    quickWebview.getAgentWeb().getJsAccessEntrace().callJs("document.getElementById(\"feedbackClick\").click()");
                                }catch (Throwable throwable){
                                    LogUtils.w(throwable);
                                }
                            }
                        },4000);
                        MyToast.show("请稍等");

                    }
                });
    }
}
