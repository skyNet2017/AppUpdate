package com.vector.update_app;

import android.text.TextUtils;

import androidx.annotation.Keep;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 版本信息
 */
@Keep
public class UpdateAppBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * update : Yes
     * new_version : xxxxx
     * version_code : 123
     * apk_url : http://cdn.the.url.of.apk/or/patch
     * update_log : xxxx
     * delta : false
     * new_md5 : xxxxxxxxxxxxxx
     * target_size : 601132
     */
    //是否有新版本
    private String update;
    //新版本号
    private String new_version;

    public int getVersionCode() {
        return version_code;
    }

    public UpdateAppBean setVersionCode(int version_code) {
        this.version_code = version_code;
        return this;
    }

    //新版本号
    private int version_code;
    //新app下载地址
    private String apk_file_url;
    //更新日志
    private String update_log;
    //配置默认更新dialog 的title
    private String update_def_dialog_title;
    //新app大小
    private String target_size;
    //是否强制更新
    private boolean constraint;
    //md5
    private String new_md5;
    //是否增量 暂时不用
    private boolean delta;
    //服务器端的原生返回数据（json）,方便使用者在hasNewApp自定义渲染dialog的时候可以有别的控制，比如：#issues/59
    private String origin_res;

    /*abtest控制*/
    private  boolean abtest_on;
    private  UpdateAppBean abtest_info;
    private  int abtest_percent;

    private  boolean abtest_by_uid;


    public void checkAbtestAndReplaceInfo(){
        boolean hasHitAbtest = hasHitAbtest();
        if(!hasHitAbtest){
            return;
        }
        UpdateAppBean abtestInfo = abtest_info;
        this.update = abtestInfo.update;
        this.update_log = abtestInfo.update_log;
        this.new_version = abtestInfo.new_version;
        this.version_code = abtestInfo.version_code;
        this.apk_file_url = abtestInfo.apk_file_url;
        this.target_size = abtestInfo.target_size;
        this.new_md5 = abtestInfo.new_md5;
        this.constraint = abtestInfo.constraint;
        this.constraint_if_below = abtestInfo.constraint_if_below;

    }
    private boolean hasHitAbtest(){
        if(abtest_on == false){
            LogUtils.i("abtest_on == false");
            return false;
        }
        if(abtest_info ==null){
            LogUtils.w("abtest on but abtest info is null!");
            return false;
        }
        if(abtest_info.abtest_percent <=0){
            LogUtils.w("abtest on but abtest_percent <=0! "+abtest_info.abtest_percent);
            return false;
        }
        String id = "";
        boolean useUid = false;
        List<String> chars = null;
        if(abtest_info.abtest_by_uid && UpdateAppManager.getParam != null){
            id = UpdateAppManager.getParam.getUid();
            if(TextUtils.isEmpty(id) || "0".equals(id)){
                LogUtils.w("未登录,使用AndroidId来随机");
                id = DeviceUtils.getAndroidID().trim().toLowerCase();

            }else {
                useUid = true;
                LogUtils.d("使用uid来随机: "+ id);
            }
        }else {
            id = DeviceUtils.getAndroidID().trim().toLowerCase();
            LogUtils.d("使用deviceId来随机: "+ id);
        }
        chars = getRandomChars(useUid,AppUtils.getAppVersionCode());
        //取最后一个字符
        String c = id.substring(id.length() - 1);

        int index = -1;
        for (int i = 0; i < chars.size(); i++) {
            if(c.equals(chars.get(i))){
                index = i;
                break;
            }
        }
        if(index ==-1){
            LogUtils.w("abtest on but last chat not leagal! "+c);
            return false;
        }
        float max = chars.size()  * abtest_info.abtest_percent / 100f;
        int maxInt = Math.round(max) -1;
       try {
           LogUtils.i(
                   "用于计算命中的id : "+id+",最后一位:"+c+",\n当前版本当前设备random字符: "+(GsonUtils.toJson(chars))+"\n推量百分比:" +abtest_info.abtest_percent+
                           "%,数组最大下标:"+maxInt+",对应字母:"+chars.get(maxInt));
       }catch (Throwable throwable){
           LogUtils.w(throwable);
       }

        if(index <= maxInt){
            LogUtils.i("abtest 命中:  "+c);
            return true;
        }else {
            LogUtils.i("abtest 未命中:  "+c);
            return false;
        }
    }

    private List<String> getRandomChars(boolean useUid, int appVersionCode) {
        try {
            String json = SPStaticUtils.getString("update_shuttle_list_"+appVersionCode+"-useUid-"+useUid,"");
            if(!json.isEmpty()){
                List<String> list = GsonUtils.fromJson(json,new TypeToken<List<String>>(){}.getType());
                if(list !=  null && !list.isEmpty()){
                    return list;
                }
            }
        }catch (Throwable throwable){
            LogUtils.w(throwable);
        }

        List<String> list = null;
        if(useUid){
            list = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")) ;
        }else{
            list = new ArrayList<>(Arrays.asList("0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","g","h","i","j","k","l","m","n",
                    "o","p","q","r","s","t","u","v","w","x","y","z")) ;
        }
        Collections.shuffle(list);
        String s = GsonUtils.toJson(list);
        SPStaticUtils.put("update_shuttle_list_"+appVersionCode+"-useUid-"+useUid,s );
        return list;
    }

    private  int constraint_if_below;
    /**********以下是内部使用的数据**********/

    //网络工具，内部使用
    private HttpManager httpManager;
    private String targetPath;
    private boolean mHideDialog;
    private boolean mShowIgnoreVersion;
    private boolean mDismissNotificationProgress;
    private boolean mOnlyWifi;

    //是否隐藏对话框下载进度条,内部使用
    public boolean isHideDialog() {
        return mHideDialog;
    }

    public void setHideDialog(boolean hideDialog) {
        mHideDialog = hideDialog;
    }

    public boolean isUpdate() {
        return !TextUtils.isEmpty(this.update) && "Yes".equals(this.update);
    }

    public HttpManager getHttpManager() {
        return httpManager;
    }

    public void setHttpManager(HttpManager httpManager) {
        this.httpManager = httpManager;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public boolean isConstraint() {
        if(constraint_if_below>0){
            if(AppUtils.getAppVersionCode() < constraint_if_below ){
                return  true;
            }
        }
        return constraint;
    }

    public UpdateAppBean setConstraint(boolean constraint) {
        this.constraint = constraint;
        return this;
    }

    public String getUpdate() {
        return update;
    }

    public UpdateAppBean setUpdate(String update) {
        this.update = update;
        return this;
    }

    public String getNewVersion() {
        return new_version;
    }

    public UpdateAppBean setNewVersion(String new_version) {
        this.new_version = new_version;
        return this;
    }

    public String getApkFileUrl() {
        return apk_file_url;
    }


    public UpdateAppBean setApkFileUrl(String apk_file_url) {
        this.apk_file_url = apk_file_url;
        return this;
    }

    public String getUpdateLog() {
        return update_log;
    }

    public UpdateAppBean setUpdateLog(String update_log) {
        this.update_log = update_log;
        return this;
    }

    public String getUpdateDefDialogTitle() {
        return update_def_dialog_title;
    }

    public UpdateAppBean setUpdateDefDialogTitle(String updateDefDialogTitle) {
        this.update_def_dialog_title = updateDefDialogTitle;
        return this;
    }

    public boolean isDelta() {
        return delta;
    }

    public void setDelta(boolean delta) {
        this.delta = delta;
    }

    public String getNewMd5() {
        return new_md5;
    }

    public UpdateAppBean setNewMd5(String new_md5) {
        this.new_md5 = new_md5;
        return this;
    }

    public String getTargetSize() {
        return target_size;
    }

    public UpdateAppBean setTargetSize(String target_size) {
        this.target_size = target_size;
        return this;
    }

    public boolean isShowIgnoreVersion() {
        return mShowIgnoreVersion;
    }

    public void showIgnoreVersion(boolean showIgnoreVersion) {
        mShowIgnoreVersion = showIgnoreVersion;
    }

    public void dismissNotificationProgress(boolean dismissNotificationProgress) {
        mDismissNotificationProgress = dismissNotificationProgress;
    }

    public boolean isDismissNotificationProgress() {
        return mDismissNotificationProgress;
    }

    public boolean isOnlyWifi() {
        return mOnlyWifi;
    }

    public void setOnlyWifi(boolean onlyWifi) {
        mOnlyWifi = onlyWifi;
    }

    public String getOriginRes() {
        return origin_res;
    }

    public UpdateAppBean setOriginRes(String originRes) {
        this.origin_res = originRes;
        return this;
    }

}
