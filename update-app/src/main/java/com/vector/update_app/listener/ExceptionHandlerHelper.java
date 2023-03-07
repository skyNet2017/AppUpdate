package com.vector.update_app.listener;

import com.blankj.utilcode.util.LogUtils;

/**
 * Created by Vector
 * on 2018/4/9.
 */
public class ExceptionHandlerHelper {
    private static  ExceptionHandler instance = new ExceptionHandler() {
        @Override
        public void onException(Exception e) {
            LogUtils.w(e);
        }
    };
    public static void init(ExceptionHandler exceptionHandler) {
        ExceptionHandler temp = instance;
        if (temp == null) {
            synchronized (ExceptionHandlerHelper.class) {
                temp = instance;
                if (temp == null) {
                    temp = exceptionHandler;
                    instance = temp;
                }
            }
        }
    }
    public static ExceptionHandler getInstance() {
        return instance;
    }
}
