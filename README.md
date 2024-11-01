
## Android 版本更新

# 更新策略: abtest功能支持

目前实现的是位于客户端的abtest.

支持根据uid来random和根据deviceId(AndroidId)来random.

对应的json配置文件:

```json
{
  "update": "Yes",
  "new_version": "1.1.00",
  "version_code":2000,
  "app_store_id": "jufjj89",
  "app_store_link": "http://kodo.hss01248.tech/ai/app-release.apk",
  "apk_file_url": "http://kodo.hss01248.tech/ai/app-release.apk",
  "file_url_mac": "http://kodo.hss01248.tech/ai/MyChatAI-release.dmg",
  "file_url_win": "http://kodo.hss01248.tech/ai/MyChatAI-release.exe",
  "file_url_ios": "http://kodo.hss01248.tech/ai/MyChatAI-release.dpa",
  "update_log": "1. android webview优化,可录音 \r\n",
  "target_size": "25M",
  "show_dialog_when_app_start":false,
  "new_md5":"",
  "constraint": false,
  "constraint_if_below": 1,
  "abtest_on":true,
  "abtest_info":{
  	"abtest_percent": 90,
    "abtest_by_uid": true,
    "update": "Yes",
    "new_version": "2.1.00",
    "version_code":2100,
    "app_store_id": "jufjj89",
    "app_store_link": "http://kodo.hss01248.tech/ai/app-release.apk",
    "apk_file_url": "http://kodo.hss01248.tech/ai/app-release.apk",
    "file_url_mac": "http://kodo.hss01248.tech/ai/MyChatAI-release.dmg",
    "file_url_win": "http://kodo.hss01248.tech/ai/MyChatAI-release.exe",
    "file_url_ios": "http://kodo.hss01248.tech/ai/MyChatAI-release.dpa",
    "update_log": "1. android webview优化,可录音abtest \r\n",
    "target_size": "26M",
    "show_dialog_when_app_start":true,
    "new_md5":"",
    "constraint": true,
    "constraint_if_below": 0
  }
}
```



# 更新策略:弹窗or红点

```java
    //app启动时检测到更新后弹窗. 如果false,则只在设置页面显示红点
    private boolean show_dialog_when_app_start = true;
```





# 策略

检查到更新后,可配置跳转到谷歌商店/外部浏览器打开/app内下载

```java
 AppUpdateUtil.setGuideToGooglePlay(true);
 AppUpdateUtil.setDownloadByBrowser(true);
```

其中内部有更细致的判断:

```java
    public static void setGuideToGooglePlay(boolean guideToGooglePlay) {
        UpdateAppManager.guideToGooglePlay = guideToGooglePlay;
        if(guideToGooglePlay){
            //检查包名是否存在:
            String url = "https://play.google.com/store/apps/details?id="+AppUtils.getAppPackageName();
            
            defaultHttpImpl.asyncGet(url, new HashMap<>(), new HttpManager.Callback() {
                @Override
                public void onResponse(String result) {

                }

                @Override
                public void onError(String error) {
                    if(!TextUtils.isEmpty(error)){
                        if(error.startsWith("404")){
                          //404代表不存在,还没有上架
                            UpdateAppManager.guideToGooglePlay = false;
                        }
                    }
                  //网络不通,也不跳谷歌商店
                    UpdateAppManager.guideToGooglePlay = false;
                }
            });

        }
    }
```



```java
public static boolean isDownloadByBrowser() {

        if(downloadByBrowser){
            return true;
        }
        if(isPermissionDeclared(Utils.getApp(), Manifest.permission.REQUEST_INSTALL_PACKAGES)){
            return false;
        }
        //如果没有声明安装权限,那么不管外面怎么设置,都跳到外部浏览器去下载
        return true;
    }
```



点击下载/按钮的逻辑:

```java
    private void installApp() {
        if(UpdateAppManager.isGuideToGooglePlay()){
           boolean success =  guideToGooglePlay();
           if(success){
               return;
           }
        }

        //跳到浏览器去下载:
        if(UpdateAppManager.isDownloadByBrowser()){
            String url = mUpdateApp.getApkFileUrl();
            openUrl(url);
            return;
        }

        if (AppUpdateUtils.appHasDownloaded(mUpdateApp)) {
            AppUpdateUtils.checkAndInstallApk( AppUpdateUtils.getAppFile(mUpdateApp));
            //安装完自杀
            //如果上次是强制更新，但是用户在下载完，强制杀掉后台，重新启动app后，则会走到这一步，所以要进行强制更新的判断。
            if (!mUpdateApp.isConstraint()) {
                dismiss();
            } else {
                showInstallBtn(AppUpdateUtils.getAppFile(mUpdateApp));
            }
        } else {
            downloadApp();
            //这里的隐藏对话框会和强制更新冲突，导致强制更新失效，所以当强制更新时，不隐藏对话框。
            if (mUpdateApp.isHideDialog() && !mUpdateApp.isConstraint()) {
                dismiss();
            }
        }
    }
```



# 服务端

### 直接使用github的文件存储功能.

https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/json/json.txt

内容为:

```json
{
  "update": "Yes",
  "new_version": "0.8.3",
   "version_code":1000,
  "apk_file_url": "https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/apk/sample-debug.apk",
  "update_log": "1，添加删除信用卡接口。\r\n2，添加vip认证。\r\n3，区分自定义消费，一个小时不限制。\r\n4，添加放弃任务接口，小时内不生成。\r\n5，消费任务手动生成。",
  "target_size": "5M",
  "new_md5":"b97bea014531123f94c3ba7b7afbaad2",
  "constraint": false
}
```

apk也直接放github上,国内可以通过jsdeliver来访问



### 也可使用蒲公英的发布和版本管理功能:

```groovy
api 'com.github.skyNet2017.AppUpdate:update-pyger:4.1.8'
```

一键发布和更新脚本:

```groovy
buildscript {
    apply from:'https://raw.githubusercontent.com/skyNet2017/AppUpdate/master/uploadToPyger.gradle?a=3'
}
```

Local.properties里配置:

```properties
pyger_api_key=xxxx
pyger_app_key=yyyy
```

那么,在Android studio的gradle面板里运行对应的uploadApk命令即可:

该工程的应用module也会自动带有app更新功能.

![image-20241101143533073](https://cdn.jsdelivr.net/gh/shuiniuhss/myimages@main/imagemac3/image-20241101143533073.png)



## 目录

* [功能介绍](#功能介绍)
* [效果图与示例 apk](#效果图与示例-apk)
* [Gradle 依赖](#Gradle依赖)
* [简单使用](#简单使用)
* [详细说明](#详细说明)
* [更新日志](#更新日志)
* [License](#license)

## 功能介绍

- [x] 实现android版本更新
- [x] 对kotlin适配，调用更简单
- [x] 自定义接口协议，可以不改变现有项目的协议就能使用
- [x] 支持get,post请求
- [x] 支持进度显示，对话框进度条，和通知栏进度条展示
- [x] 支持后台下载
- [x] 支持强制更新
- [x] 支持简单主题色配置(可以自动从顶部图片提取主色)
- [x] 支持自定义对话框（可以监听下载进度）
- [x] 支持静默下载（可以设置wifi状态下）
- [x] 支持android7.0
- [ ] **以下是fork后的修改**
- [x] **文案遵循Android strings.xml配置方式**
- [x] **解除对存储权限的依赖**
- [x] **适配Android12**

## 效果图与示例 apk

<img src="https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/image/example_01.png?raw=true" width="1000">

<img src="https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/image/example_02.png?raw=true" width="1000">

<img src="https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/image/example_03.png?raw=true" width="1000">

<img src="https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/image/example_05.png" width="1000">

<img src="https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/image/example_06.png" width="1000">


[点击下载 Demo.apk](https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/apk/sample-debug.apk) 或扫描下面的二维码安装

![Demo apk文件二维](https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/image/1498810770.png)



## Gradle 依赖

**java方式引用**

```gradle
dependencies {
    compile 'com.qianwen:update-app:3.5.2'
}
```

[![Download](https://api.bintray.com/packages/qianwen/maven/update-app/images/download.svg) ](https://bintray.com/qianwen/maven/update-app/_latestVersion) [![API](https://img.shields.io/badge/API-14%2B-orange.svg?style=flat)](https://android-arsenal.com/api?level=14) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![GitHub stars](https://img.shields.io/github/stars/WVector/AppUpdate.svg?style=plastic&label=Star) ](https://github.com/WVector/AppUpdate)


**kotlin方式引用**

```gradle
dependencies {
    compile 'com.qianwen:update-app-kotlin:1.2.3'
}
```

[![Download](https://api.bintray.com/packages/qianwen/maven/update-app-kotlin/images/download.svg) ](https://bintray.com/qianwen/maven/update-app/_latestVersion) [![API](https://img.shields.io/badge/API-14%2B-orange.svg?style=flat)](https://android-arsenal.com/api?level=14) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![GitHub stars](https://img.shields.io/github/stars/WVector/AppUpdate.svg?style=plastic&label=Star) ](https://github.com/WVector/AppUpdate)


## 简单使用



1,java方式

```java
	new UpdateAppManager
                .Builder()
                //当前Activity
                .setActivity(this)
                //更新地址
                .setUpdateUrl(mUpdateUrl)
                //实现httpManager接口的对象
                .setHttpManager(new UpdateAppHttpUtil())
                .build()
                .update();
```
2,kotlin方式

```kotlin
	updateApp(mUpdateUrl, UpdateAppHttpUtil()).update()
```

## 详细说明

- [java方式](java.md)
- [kotlin方式](kotlin.md)

#### 进度条使用的是代码家的「[NumberProgressBar](https://github.com/daimajia/NumberProgressBar)」

## 更新日志

kotlin版本是依赖java版本的，所以java版本的问题kotlin自然修复


v3.5.2

	1，修复下载过程中，关闭对话框不能自动安装问题。

v3.5.1

	1，修复bug

v3.5.0

	1，优化强制更新 

v3.4.8 

	1,修复bug

v3.4.7 

	1,优化 APP 安装的问题

v3.4.6 

	1,优化 APP 安装的问题

v3.4.5

	1，增加全局异常捕获方法
	
	                .handleException(new ExceptionHandler() 						{
	                @Override
	                public void onException(Exception e) {
	
	                }
	            })

v3.4.4

	1，修复bug
[bug](https://github.com/WVector/AppUpdate/pull/68)

v3.4.3

	1，修复bug
[bug](https://github.com/WVector/AppUpdate/pull/67)

v3.4.2

	1,修复bug
[bug](https://github.com/WVector/AppUpdate/pull/66)

v3.4.1

	1,给插件使用者更多的配置和开启一些钩子方便适配不同的业务需求
	2,适配android8.0

感谢[Jiiiiiin](https://github.com/Jiiiiiin)对项目的维护

v3.4.0

	1,修复  
[issues#59](https://github.com/WVector/AppUpdate/issues/59)



v3.3.9

	1,适配android8.0的通知和安装未知来源的app

感谢[typ0520](https://github.com/typ0520)对项目的维护

v3.3.8

	1,增加存储空间权限申请

V3.3.7
	
	1,修改默认安装包下载路径为download/packageName

感谢[bean-liu](https://github.com/bean-liu)对项目的维护

V3.3.6
	
	1,去掉对下载路径前缀的校验。
[https://github.com/WVector/AppUpdate/issues/26](https://github.com/WVector/AppUpdate/issues/26)

V3.3.5

	1，修复升级对话框布局中的问题。
	2，修复静默下载，关闭更新弹窗 再点击更新 一直显示的问题。
[https://github.com/WVector/AppUpdate/issues/21](https://github.com/WVector/AppUpdate/issues/21)

V3.3.4

	1，修复对话框更新内容过多，升级按钮被挤压的问题。
	2，去掉自动从图片提取颜色的功能， 通过.setThemeColor()设置按钮和精度条颜色，
	3，兼容compileSdkVersion <25

V3.3.3

	1，修复下载路径是重定向路径不能下载的问题

V3.3.2

	1，修复正在下载时，返回桌面报错的问题
[https://github.com/WVector/AppUpdate/issues/14](https://github.com/WVector/AppUpdate/issues/14)

V3.3.1

	1，修复对话框外可以点击的问题

V3.3.0 

	1，可以设置不显示通知栏进度条。
	2，可以设置忽略版本。 
	3，优化下载时页面卡的问题（由于下载进度回调调用频繁，造成ui线程阻塞）。
	4，可以静默下载，类似网易云音乐，并且设置wifi状态下。

 V3.2.9 

	1，新增自定义对话框。
	2，适配kotlin，写法更简单。 







# 基于蒲公英网站的app更新和用户反馈系统

## app更新

```groovy
   api 'com.github.skyNet2017.AppUpdate:update-default:4.0.6'
```



```java
PygerAppUpdateUtil.doUpdate("key", "token",new ExceptionHandler() {
            @Override
            public void onException(Exception e) {
                e.printStackTrace();
```

![image-20230130120530421](https://cdn.jsdelivr.net/gh/shuiniuhss/myimages@main/imagemac3/image-20230130120530421.png)

## 用户反馈

```groovy
   api 'com.github.skyNet2017.AppUpdate:feedback:4.0.6'
```



```java
FeedbackUtil.showPygerFeedback("https://www.pgyer.com/YVeW");
```



![image-20230130120502987](https://cdn.jsdelivr.net/gh/shuiniuhss/myimages@main/imagemac3/image-20230130120502987.png)





## License

   	Copyright 2017 千匍
   	
   	Licensed under the Apache License, Version 2.0 (the "License");
   	you may not use this file except in compliance with the License.
   	You may obtain a copy of the License at
   	
   	   http://www.apache.org/licenses/LICENSE-2.0
   	
   	Unless required by applicable law or agreed to in writing, software
   	distributed under the License is distributed on an "AS IS" BASIS,
   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   	See the License for the specific language governing permissions and
   	limitations under the License.