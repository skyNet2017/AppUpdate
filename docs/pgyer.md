# 基于蒲公英的 App 更新与用户反馈

将[蒲公英](https://www.pgyer.com/)作为更新管理平台时，可使用本仓库提供的 `update-pyger` / `update-default` / `feedback` 模块，完成发布、版本检测与用户反馈。

## 依赖

```groovy
api 'com.github.skyNet2017.AppUpdate:update-pyger:4.1.8'
```

或使用默认封装：

```groovy
api 'com.github.skyNet2017.AppUpdate:update-default:4.0.6'
```

## 一键发布与更新脚本

```groovy
buildscript {
    apply from:'https://raw.githubusercontent.com/skyNet2017/AppUpdate/master/uploadToPyger.gradle?a=3'
}
```

在 `local.properties` 中配置：

```properties
pyger_api_key=xxxx
pyger_app_key=yyyy
```

配置完成后，在 Android Studio 的 Gradle 面板中运行对应的 `uploadApk` 命令即可。该工程的应用 module 也会自动带有 App 更新功能。

![gradle uploadApk](https://cdn.jsdelivr.net/gh/shuiniuhss/myimages@main/imagemac3/image-20241101143533073.png)

## App 更新

```java
PygerAppUpdateUtil.doUpdate("key", "token", new ExceptionHandler() {
    @Override
    public void onException(Exception e) {
        e.printStackTrace();
    }
});
```

![蒲公英更新示意](https://cdn.jsdelivr.net/gh/shuiniuhss/myimages@main/imagemac3/image-20230130120530421.png)

## 用户反馈

```groovy
api 'com.github.skyNet2017.AppUpdate:feedback:4.0.6'
```

```java
FeedbackUtil.showPygerFeedback("https://www.pgyer.com/YVeW");
```

![蒲公英反馈示意](https://cdn.jsdelivr.net/gh/shuiniuhss/myimages@main/imagemac3/image-20230130120502987.png)
