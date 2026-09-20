# 基于自建 Zealot 的 App 更新与发版

将 [Zealot](https://github.com/tryzealot/zealot)（本环境：https://appstore.timefly.art）作为内测分发与检查更新平台时，使用本仓库 `update-zealot` + `uploadToZealot.gradle`（单 app）或 `uploadToZealotModule.gradle`（多 app）。

> 与蒲公英 / 七牛方案二选一即可，**不要**在同一工程同时 `apply` Zealot 脚本与 `uploadToPyger.gradle` / `uploadToQiniu.gradle`；也**不要**同时 apply 两个 Zealot 脚本。

## 一键集成

按工程里 **application module 数量** 二选一（**不要**同时 apply 两个脚本，也勿与蒲公英 / 七牛脚本混用）。

### A. 单 application module（根工程 apply）

根工程 `build.gradle`：

```groovy
buildscript {
    apply from: 'https://raw.githubusercontent.com/skyNet2017/AppUpdate/master/uploadToZealot.gradle'
    // 本仓库本地调试也可：
    // apply from: 'uploadToZealot.gradle'
}
```

`local.properties`（勿提交 git）：

```properties
zealot_endpoint=https://appstore.timefly.art
zealot_channel_key=渠道详情里的 Key
zealot_token=用户页底部 API Key
# 可选：jitpack 版本（外部工程）
# zealot_lib_version=5.0.0
# 本仓库调试强制用本地模块：
# zealot_use_local=true
```

### B. 多 application module（各 module 内 apply）

每个应用 module 的 `build.gradle`：

```groovy
apply plugin: 'com.android.application'

ext.zealot_channel_key = '该应用自己的渠道 Key'  // 必填；不是 slug
// ext.zealot_endpoint = 'https://appstore.timefly.art'  // 可选，覆盖 local.properties
// ext.zealot_lib_version = '5.0.0'

apply from: 'https://raw.githubusercontent.com/skyNet2017/AppUpdate/master/uploadToZealotModule.gradle'
// 本仓库本地调试也可：
// apply from: rootProject.file('uploadToZealotModule.gradle')
```

`local.properties` 只放共享上传凭证（**不要**再写全局 `zealot_channel_key`）：

```properties
zealot_endpoint=https://appstore.timefly.art
zealot_token=用户页底部 API Key
# zealot_lib_version=5.0.0
# zealot_use_local=true
```

根工程 **不要** `apply` `uploadToZealot.gradle`，否则会重复加依赖 / 重复建任务。

本仓库 `sample` 已按方案 B 接入：`apply from: rootProject.file('uploadToZealotModule.gradle')`，并将 `local.properties` 中的 `zealot_channel_key` 写入 `ext`（演示用，避免把 key 提交进仓库；真实多 app 工程请在各 module 直接写死各自的 key）。

### 配置项说明

上传实现与蒲公英脚本相同：`HttpURLConnection` multipart；changelog 等文本字段用 **UTF-8** 写入（不用 `DataOutputStream.writeBytes`，避免中文损坏）。

| 配置项 | 是否进 APK | 单 module（A） | 多 module（B） |
|--------|------------|----------------|----------------|
| `zealot_endpoint` | 是（BuildConfig） | `local.properties` | `local.properties` 或 module `ext` |
| `zealot_channel_key` | 是（BuildConfig） | `local.properties` | **各 module `ext.zealot_channel_key`** |
| `zealot_token` | **否** | `local.properties` | `local.properties`（共享） |

配置后：

1. 应用 module 自动依赖 `update-zealot`，启动约 1.2s 后检查更新并弹窗  
2. Android Studio Gradle 面板 → `uploadApk` → `uploadApkOf{Module}{Variant}` 一键 **clean → assemble → 上传**  

找包路径（取最新匹配的 `.apk`）：

| 场景 | 目录 |
|------|------|
| 原生 / AGP | `{app}/build/outputs/apk/{debug\|release}` |
| Flutter | `{flutterRoot}/build/app/outputs/flutter-apk` 与 `.../apk`（如 `mobile/build/app/outputs`） |

若 APK 只在 Flutter 输出目录，会再复制一份到 AGP 常规 `build/outputs/apk` 便于核对，上传用找到的文件。

本仓库根工程若包含 `:update-zealot`，脚本默认走 `project(':update-zealot')`（无需等 jitpack tag）。外部工程走 `com.github.skyNet2017.AppUpdate:update-zealot:5.0.0`（需打对应 tag）。

## slug 与 channel_key

| 用途 | 用什么 |
|------|--------|
| 安装页 / 二维码 | slug，如 `https://appstore.timefly.art/JLM4A` |
| 检查更新 / 上传 API | **channel_key**（渠道详情页复制） |

后台概览页 `.../JLM4A/overview` **不是** API 地址。

## API 对照

| 能力 | 请求 |
|------|------|
| 检查最新版 | `GET {endpoint}/api/apps/latest?channel_key=...&release_version={versionName}&build_version={versionCode}` |
| 发版上传 | `POST {endpoint}/api/apps/upload?token=...` multipart：`channel_key`、`file`、`changelog`、`source=gradle` |

客户端比较远端 `build_version` 与本机 `versionCode`，更大则弹更新。

**下载安装（与蒲公英一致，应用内完成）：**

1. 接口返回的 `install_url` 多为 `/download/releases/{id}`（会 302），库内会 `HEAD` 跟随重定向，解析出真正的 `.apk` 直链  
2. 强制 `downloadByBrowser=false` / `guideToGooglePlay=false`，由 `DownloadService` 应用内下载后调起系统安装器  
3. 首次安装需系统「允许来自此来源的应用」授权（跳系统设置，不是浏览器）；宿主需合并 `REQUEST_INSTALL_PACKAGES`（`update-app` 已声明）

**注意（Zealot 6.2.x）：** `GET /api/apps/latest` 若带 `release_version` / `build_version` 会服务端 500（`Release.find_since_version` NameError）。本库只传 `channel_key`，响应里取 `releases[0]` 再本地比较。

## 手动触发检查更新

```java
ZealotAppUpdateUtil.doUpdate();

// 或显式传参
ZealotAppUpdateUtil.doUpdate(
    "https://appstore.timefly.art",
    "你的 channel_key",
    e -> LogUtils.w(e)
);
```

## 依赖（不经过脚本时）

```groovy
api 'com.github.skyNet2017.AppUpdate:update-zealot:5.0.0'
// 并自行写入 BuildConfig.zealot_endpoint / zealot_channel_key
```

## 发版给其他 App 用

1. 合并本仓库改动后打 git tag（如 `5.0.0`）并 push  
2. 外部工程：单 app 用远程 `uploadToZealot.gradle`；多 app 用远程 `uploadToZealotModule.gradle` + 各 module `ext.zealot_channel_key`  
3. 配置 `local.properties` 中的 `zealot_token`（及可选 `zealot_endpoint`）  
4. 若 jitpack 尚未构建成功，可临时 `zealot_lib_version` 指向已有 tag，或先用本仓库 `composite` / `mavenLocal`
