# 普通 JSON 配置文件方式的 App 更新

通过托管一份版本信息 JSON（如 GitHub raw / 自建 CDN / 七牛云），客户端请求该地址后按约定字段解析并执行更新。无需依赖蒲公英等第三方发布平台。

> 与蒲公英方案二选一即可，**不要**在同一工程同时 `apply` `uploadToPyger.gradle` 与 `uploadToQiniu.gradle`。

## 七牛云一键集成与发布（推荐）

仿照蒲公英脚本，将 APK 与 `update.json` 托管到七牛 Kodo，Gradle 一键打包上传，客户端零配置检测更新。

### 1. 引入脚本

```groovy
buildscript {
    apply from: 'https://raw.githubusercontent.com/skyNet2017/AppUpdate/master/uploadToQiniu.gradle'
}
```

脚本会自动为 `com.android.application` 模块：

- 注入依赖 `com.github.skyNet2017.AppUpdate:update-default:4.1.9`
- 写入 `BuildConfig.update_json_url`（固定 JSON 地址）
- 注册 `uploadApk` 组任务

### 2. 配置凭证（勿提交到仓库）

复制仓库根目录示例文件 [`qiniu.local.properties.example`](../qiniu.local.properties.example) 中的字段到 `local.properties`（或 `gradle.properties`）。

**上传域名 ≠ 下载域名**：

| 用途 | 配置项 | 典型值 | 说明 |
|------|--------|--------|------|
| 上传 | `qiniu_upload_host` | `https://upload.qiniup.com` | Gradle 向七牛 POST 文件；跨区桶改对应区域 upload host |
| 下载 / CDN | `qiniu_cdn_domain` | `https://kodo.example.com` | 空间绑定域名；写入 `apk_file_url` 与 `BuildConfig.update_json_url` |

```properties
qiniu_ak=你的AccessKey
qiniu_sk=你的SecretKey
qiniu_bucket=你的空间名
# 下载访问域名（不是上传域名）
qiniu_cdn_domain=https://kodo.example.com
# 可选：上传域名，默认 https://upload.qiniup.com
# qiniu_upload_host=https://upload.qiniup.com
# 可选
# qiniu_key_prefix=appupdate
# qiniu_app_id=覆盖默认 applicationId（多渠道特殊包名时用）
# update_log=自定义更新日志（默认取最近一条 git commit message）
# constraint=false
# constraint_if_below=
```

兼容旧配置名：`qiniu_domain` 仍可作为 `qiniu_cdn_domain` 的别名读取。

### 3. 一键发布

在 Android Studio Gradle 面板中运行对应 module 的：

`uploadApk` → `uploadApkOfXxxRelease`（或 Debug）

流程：`assemble` → 上传版本化 APK → 生成并**覆盖**上传 `update.json`。

本地 dry-run（只打包并生成 JSON，**不上传**）：

```bash
./gradlew :sample:uploadApkOfSampleDebug -Pqiniu_dry_run=true -Pqiniu_cdn_domain=https://kodo.hss01248.tech
```

生成的 JSON 在 `sample/build/qiniu-update/update.json`。

对象键约定：

| 类型 | Key | 说明 |
|------|-----|------|
| APK | `{prefix}/{packageName}/{versionName}-{versionCode}.apk` | 版本化，避免 CDN 缓存旧包 |
| JSON | `{prefix}/{packageName}/update.json` | 固定地址，每次发版覆盖 |

公开 URL 示例：`https://kodo.example.com/appupdate/com.example.app/update.json`

### 4. 客户端调用

```java
// Application / 启动页：读 BuildConfig.update_json_url
AppUpdateUtil.doUpdate();

// 设置页点击检查更新
AppUpdateUtil.updateByClickBtn();
```

也可继续手传 URL：`AppUpdateUtil.doUpdate("https://.../update.json", true)`。

### 5. 流量与缓存（约 10GB/月免费额度）

- 流量几乎全部来自 **APK 下载**；JSON 体积可忽略。
- 粗算：50MB 安装包约可完整下载 **200 次** ≈ 10GB。
- 建议在七牛 CDN 对 `*.json` 配置短缓存或 `no-cache`，避免客户端读到旧配置；版本化 APK 可设较长缓存。
- 适合内测 / 小流量分发；量大时再升级按量计费或换更大带宽方案。

## 服务端：托管 JSON

### 直接使用 GitHub 文件存储

示例地址：

https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/json/json.txt

内容示例：

```json
{
  "update": "Yes",
  "new_version": "0.8.3",
  "version_code": 1000,
  "apk_file_url": "https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/apk/sample-debug.apk",
  "update_log": "1，添加删除信用卡接口。\r\n2，添加vip认证。\r\n3，区分自定义消费，一个小时不限制。\r\n4，添加放弃任务接口，小时内不生成。\r\n5，消费任务手动生成。",
  "target_size": "5M",
  "new_md5": "b97bea014531123f94c3ba7b7afbaad2",
  "constraint": false
}
```

APK 也可直接放在 GitHub 上；国内可通过 jsDelivr 加速访问。

更完整的默认协议说明见 [java.md](java.md) / [kotlin.md](kotlin.md)。

## 更新策略：ABTest

目前实现的是位于客户端的 ABTest。

支持根据 `uid` random，以及根据 `deviceId`（AndroidId）random。

对应的 JSON 配置文件：

```json
{
  "update": "Yes",
  "new_version": "1.1.00",
  "version_code": 2000,
  "app_store_id": "jufjj89",
  "app_store_link": "http://kodo.hss01248.tech/ai/app-release.apk",
  "apk_file_url": "http://kodo.hss01248.tech/ai/app-release.apk",
  "file_url_mac": "http://kodo.hss01248.tech/ai/MyChatAI-release.dmg",
  "file_url_win": "http://kodo.hss01248.tech/ai/MyChatAI-release.exe",
  "file_url_ios": "http://kodo.hss01248.tech/ai/MyChatAI-release.dpa",
  "update_log": "1. android webview优化,可录音 \r\n",
  "target_size": "25M",
  "show_dialog_when_app_start": false,
  "new_md5": "",
  "constraint": false,
  "constraint_if_below": 1,
  "abtest_on": true,
  "abtest_info": {
    "abtest_percent": 90,
    "abtest_by_uid": true,
    "update": "Yes",
    "new_version": "2.1.00",
    "version_code": 2100,
    "app_store_id": "jufjj89",
    "app_store_link": "http://kodo.hss01248.tech/ai/app-release.apk",
    "apk_file_url": "http://kodo.hss01248.tech/ai/app-release.apk",
    "file_url_mac": "http://kodo.hss01248.tech/ai/MyChatAI-release.dmg",
    "file_url_win": "http://kodo.hss01248.tech/ai/MyChatAI-release.exe",
    "file_url_ios": "http://kodo.hss01248.tech/ai/MyChatAI-release.dpa",
    "update_log": "1. android webview优化,可录音abtest \r\n",
    "target_size": "26M",
    "show_dialog_when_app_start": true,
    "new_md5": "",
    "constraint": true,
    "constraint_if_below": 0
  }
}
```

## 更新策略：弹窗 or 红点

```java
// app启动时检测到更新后弹窗。如果 false，则只在设置页面显示红点
private boolean show_dialog_when_app_start = true;
```

对应 JSON 字段：`show_dialog_when_app_start`。

## 更新策略：跳转商店 / 浏览器 / App 内下载

检查到更新后，可配置跳转到谷歌商店、外部浏览器打开，或 App 内下载：

```java
AppUpdateUtil.setGuideToGooglePlay(true);
AppUpdateUtil.setDownloadByBrowser(true);
```

其中内部有更细致的判断：

```java
public static void setGuideToGooglePlay(boolean guideToGooglePlay) {
    UpdateAppManager.guideToGooglePlay = guideToGooglePlay;
    if (guideToGooglePlay) {
        // 检查包名是否存在:
        String url = "https://play.google.com/store/apps/details?id=" + AppUtils.getAppPackageName();

        defaultHttpImpl.asyncGet(url, new HashMap<>(), new HttpManager.Callback() {
            @Override
            public void onResponse(String result) {

            }

            @Override
            public void onError(String error) {
                if (!TextUtils.isEmpty(error)) {
                    if (error.startsWith("404")) {
                        // 404 代表不存在，还没有上架
                        UpdateAppManager.guideToGooglePlay = false;
                    }
                }
                // 网络不通，也不跳谷歌商店
                UpdateAppManager.guideToGooglePlay = false;
            }
        });
    }
}
```

```java
public static boolean isDownloadByBrowser() {
    if (downloadByBrowser) {
        return true;
    }
    if (isPermissionDeclared(Utils.getApp(), Manifest.permission.REQUEST_INSTALL_PACKAGES)) {
        return false;
    }
    // 如果没有声明安装权限，那么不管外面怎么设置，都跳到外部浏览器去下载
    return true;
}
```

点击下载/按钮的逻辑：

```java
private void installApp() {
    if (UpdateAppManager.isGuideToGooglePlay()) {
        boolean success = guideToGooglePlay();
        if (success) {
            return;
        }
    }

    // 跳到浏览器去下载:
    if (UpdateAppManager.isDownloadByBrowser()) {
        String url = mUpdateApp.getApkFileUrl();
        openUrl(url);
        return;
    }

    if (AppUpdateUtils.appHasDownloaded(mUpdateApp)) {
        AppUpdateUtils.checkAndInstallApk(AppUpdateUtils.getAppFile(mUpdateApp));
        // 安装完自杀
        // 如果上次是强制更新，但是用户在下载完，强制杀掉后台，重新启动 app 后，则会走到这一步，所以要进行强制更新的判断。
        if (!mUpdateApp.isConstraint()) {
            dismiss();
        } else {
            showInstallBtn(AppUpdateUtils.getAppFile(mUpdateApp));
        }
    } else {
        downloadApp();
        // 这里的隐藏对话框会和强制更新冲突，导致强制更新失效，所以当强制更新时，不隐藏对话框。
        if (mUpdateApp.isHideDialog() && !mUpdateApp.isConstraint()) {
            dismiss();
        }
    }
}
```
