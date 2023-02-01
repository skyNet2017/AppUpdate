# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\android\android-sdk-windows/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in request.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# these Content Provider: cyp 2016/4/29

# 混淆关键字说明
# keep  保留类和类中的成员，防止它们被混淆或移除。
# keepnames 保留类和类中的成员，防止它们被混淆，但当成员没有被引用时会被移除。
# keepclassmembers 只保留类中的成员，防止它们被混淆或移除。
# keepclassmembernames 只保留类中的成员，防止它们被混淆，但当成员没有被引用时会被移除。
# keepclasseswithmembers 保留类和类中的成员，防止它们被混淆或移除，前提是指名的类中的成员必须存在，如果不存在则还是会混淆。
# keepclasseswithmembernames 保留类和类中的成员，防止它们被混淆，但当成员没有被引用时会被移除，前提是指名的类中的成员必须存在，如果不存在则还是会混淆。

# -dontwarn 对包下的代码不警告
# eg: -dontwarn android.support.** 表示对android.support包下的代码不警告，因为support包中有很多代码都是在高版本中使用的，
# 如果我们的项目指定的版本比较低在打包时就会给予警告。不过support包中所有的代码都在版本兼容性上做足了判断，
# 因此不用担心代码会出问题，所以直接忽略警告就可以了。

# proguard中的通配符说明
# <field> 匹配类中的所有字段
# <method> 匹配类中的所有方法
# <init> 匹配类中的所有构造函数
# * 匹配任意长度字符，但不含包名分隔符(.)。比如说我们的完整类名是com.example.test.MyActivity，使用com.*，或者com.exmaple.*都是无法匹配的，因为*无法匹配包名中的分隔符，正确的匹配方式是com.exmaple.*.*，或者com.exmaple.test.*，这些都是可以的。但如果你不写任何其它内容，只有一个*，那就表示匹配所有的东西
# ** 匹配任意长度字符，并且包含包名分隔符(.)。比如proguard-android.txt中使用的-dontwarn android.support.**就可以匹配android.support包下的所有内容，包括任意长度的子包。
# *** 匹配任意参数类型。比如void set*(***)就能匹配任意传入的参数类型，*** get*()就能匹配任意返回值的类型。
# … 匹配任意长度的任意类型参数。比如void test(…)就能匹配任意void test(String a)或者是void test(int a, String b)这些方法。

# 压缩等级
-optimizationpasses 5
# 表示混淆时不使用大小写混合类名。
-dontusemixedcaseclassnames
# 表示不跳过library中的非public的类。
-dontskipnonpubliclibraryclasses
# 表示打印混淆的详细信息。
-verbose
# 表示不进行优化，建议使用此选项，因为根据proguard-android-optimize.txt中的描述，
# 优化可能会造成一些潜在风险，不能保证在所有版本的Dalvik上都正常运行。
-dontoptimize
# 表示不进行预校验。这个预校验是作用在Java平台上的，
# Android平台上不需要这项功能，去掉之后还可以加快混淆速度。
-dontpreverify
# 表示对注解中的参数进行保留
-keepattributes
# 忽略混淆时的一些警告
-ignorewarnings
# 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!method/removal/*

# 保证Android的一些基础类库不被混淆
-keep class android.** {*; }
-keep public class * extends android.view
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.pm
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keep class com.xuexiang.xui.widget.edittext.materialedittext.** { *; }
# 保持源码的行号与信息
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,Exceptions,InnerClasses,Signature, *Annotation*, *JavascriptInterface*

# 表示不混淆任何包含native方法的类名以及native方法名
-keepclasseswithmembernames class * {
    native <methods>;
}

# 表示不混淆任何一个View中的setXxx()和getXxx()方法，因为属性动画需要有相应的setter和getter的方法实现，混淆了就无法工作了。
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# 表示不混淆Activity中参数是View的方法，因为有这样一种用法，在XML中配置android:onClick=”buttonClick”属性，
# 当用户点击该按钮时就会调用Activity中的buttonClick(View view)方法，如果这个方法被混淆的话就找不到了。
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# 表示不混淆枚举中的values()和valueOf()方法
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# 保留序列化。
# 比如我们要向activity传递对象使用了Serializable接口的时候，这时候这个类及类里面的所有内容都不能混淆。
# 这里如果项目有用到序列化和反序列化要加上这句。
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable { *; }

# 表示不混淆R文件中的所有静态字段，我们都知道R文件是通过字段来记录每个资源的id的，
# 字段名要是被混淆了，id也就找不着了。
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 项目使用了WebView与JS，保持webview的类和类中的成员不被混淆
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
-keep public class io.xx.xx.common.webview.xxJsObj
-keep public class * implements io.xx.xx.common.webview.xxJsObj
-keepclassmembers class io.xx.xx.common.webview.xxJsObj {
    <methods>;
}

# 保持自定义控件类中含两个参数的方法不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# 保持自定义控件类中含三个参数的方法不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep public class com.hss01248.finalcompress.R$*{
      public static final int *;
}

-keep public class com.sznq.finalcompress.R$*{
      public static final int *;
}

# 保持greendao中自定生成的class文件不被混淆
-keep public class io.xx.xx.persistence.** {
     public static <fields>;
}

# 表示不混淆枚举中的values()和valueOf()方法
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 解决support.v4包与新版不兼容的问题
-dontwarn android.support.**
-dontwarn **CompatHoneycomb
-dontwarn **CompatHoneycombMR2
-dontwarn **CompatCreatorHoneycombMR2
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

# -------------------------------------------------
# 这里开始保留lib目录和remote下的第三方类库的类和成员 |
# -------------------------------------------------

# 这是保持support.v4版与新版的兼容
-dontwarn **CompatHoneycomb
-dontwarn **CompatHoneycombMR2
-dontwarn **CompatCreatorHoneycombMR2
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-dontwarn com.loopj.android.http.**
-keep class com.loopj.android.http.**{*;}

-dontwarn de.greenrobot.dao.**
-keep class de.greenrobot.dao.**{*;}

-dontwarn com.lhh.apst.library.**
-keep class com.lhh.apst.library.**{*;}

#Firebase
-keepnames class com.firebase.** { *; }
-dontwarn io.xx.xx.pushservice.**
-keep class io.xx.xx.pushservice.**{*;}

-keepclassmembers class ** {
   public void onEvent*(**);
}

-keep class com.facebook.android.*
-keep class android.webkit.WebViewClient
-keep class * extends android.webkit.WebViewClient
-keepclassmembers class * extends android.webkit.WebViewClient {
    <methods>;
}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.**{*;}
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-dontwarn io.xx.xx.entity
-keep class io.xx.xx.entity.**{*;}

-keep class io.xx.xx.module.area.model.** { *; }
#-keep public class * extends io.xx.xx.entity.BaseResponse { *; }
#-keepclassmembers class * extends io.xx.xx.entity.BaseResponse { *; }
-keep class io.xx.xx.module.item.model.** { *; }

-keep class io.xx.xx.module.creditscore.bean.EcommerceListResultBean{*;}
-keep class io.xx.xx.module.creditscore.bean.EcommerceListResultBean$*{*;}


##---------------End: proguard configuration for Gson  -------

-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }

# rxjava,rxandroid
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

-dontwarn com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.**{*;}

# Don't warn about removed methods from AppCompat
-dontwarn android.support.v4.app.NotificationCompat*

-dontwarn java.lang.invoke.*

# LinkedIn
-keep class com.linkedin.** { *; }
-keepattributes Signature

-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**
# okhttp
#-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-keepclassmembers class * extends android.webkit.WebChromeClient{
       public void openFileChooser(...);
}

#手动启用support keep注解
-dontskipnonpubliclibraryclassmembers
-printconfiguration
-keep,allowobfuscation @interface android.support.annotation.Keep
-keep @androidx.annotation.Keep class *
-keep @android.support.annotation.Keep class *
-keepclassmembers class * {
    @android.support.annotation.Keep *;
}
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
-dontwarn io.xx.xx.module.recharge.bean.**
-keep class io.xx.xx.module.recharge.bean.** {*;}
-dontwarn io.xx.xx.module.purchase.bean.**
-keep class io.xx.xx.module.purchase.bean.** {*;}
# utilcode
-keep class com.blankj.utilcode.** { *; }
-keepclassmembers class com.blankj.utilcode.** { *; }
-dontwarn com.blankj.utilcode.**

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}

#okgo
-dontwarn com.lzy.okgo.**
-keep class com.lzy.okgo.**{*;}

#okrx
-dontwarn com.lzy.okrx.**
-keep class com.lzy.okrx.**{*;}

#okrx2
-dontwarn com.lzy.okrx2.**
-keep class com.lzy.okrx2.**{*;}

#okserver
-dontwarn com.lzy.okserver.**
-keep class com.lzy.okserver.**{*;}

-keep class com.jph.takephoto.** { *; }
-dontwarn com.jph.takephoto.**

-keep class com.darsh.multipleimageselect.** { *; }
-dontwarn com.darsh.multipleimageselect.**

-keep class com.soundcloud.android.crop.** { *; }
-dontwarn com.soundcloud.android.crop.**

-dontwarn com.hss01248.dialog.**
-keep class com.hss01248.dialog.**{*;}
-keepclassmembers class com.hss01248.dialog.** { *; }

-dontwarn com.hss01248.dialog.view.GifMovieView
-keep class com.hss01248.dialog.view.GifMovieView
-keepclassmembers class com.hss01248.dialog.view.GifMovieView { *; }
-keepattributes InnerClasses,Signature
-keepattributes *Annotation*

-keep class com.xx.baselibrary.utils.callback.ProguardDisable
-keepnames class * implements com.xx.baselibrary.utils.callback.ProguardDisable
-keepclassmembers class * implements com.xx.baselibrary.utils.callback.ProguardDisable { *; }



##---------------start: proguard configuration for sensorsdata  -------
-dontwarn com.sensorsdata.analytics.android.**
-keep class com.sensorsdata.analytics.android.** {
*;
}
-keep class **.R$* {
    <fields>;
}
-keep class io.xx.xx.shenceanalysis.** { *; }
-keep public class * extends android.content.ContentProvider
-keepnames class * extends android.view.View

-keep class * extends android.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
-keep class android.support.v4.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
-keep class * extends android.support.v4.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}

# 如果使用了 DataBinding
#-dontwarn android.databinding.**
#-keep class android.databinding.** { *; }
#-keep class io.xx.xx.databinding.** {
#    <fields>;
#    <methods>;
#}
##---------------end: proguard configuration for sensorsdata  -------
-keep class cn.qqtheme.framework.entity.** { *;}

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

# 二维码
-keep class com.hss01248.xxqr.xxQRResultBean.**{*;}
-keep class com.hss01248.xxqr.GroupedTitlebar.**{*;}
-keep class com.hss01248.xxqr.xxQRResultBean{*;}
-keep class com.hss01248.xxqr.GroupedTitlebar{*;}
-keep class com.hss01248.xxqr.QRTextStyleConfig.**{*;}
-keep class net.sourceforge.zbar.ImageScanner { *; }
-keep class net.sourceforge.zbar.Image { *; }
-keep class net.sourceforge.zbar.SymbolSet { *; }
-keep class net.sourceforge.zbar.Symbol { *; }

-keep class com.hss01248.xxqr.LoginFromQREvent { *; }
-dontwarn com.hss01248.xxqr.LoginFromQREvent
-keep class com.xx.http.callback.LoginEventForNetUtil { *; }
-dontwarn com.xx.http.callback.LoginEventForNetUtil


#bean
-keep class com.xx.xxmerchants.common.bean.** { *; }
-dontwarn com.xx.xxmerchants.common.bean.**

-keep class com.xx.xxmerchants.common.event.** { *; }
-dontwarn com.xx.xxmerchants.common.event.**

-keep class com.xx.xxmerchants.module.login.model.beans.** { *; }
-dontwarn com.xx.xxmerchants.module.login.model.beans.**

-keep class com.xx.xxmerchants.module.mine.bean.** { *; }
-dontwarn com.xx.xxmerchants.module.mine.bean.**

-keep class com.xx.xxmerchants.module.wallet.bean.** { *; }
-dontwarn com.xx.xxmerchants.module.wallet.bean.**

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keep class com.xx.http.callback.S3Info { *; }
-keepclassmembers class com.xx.http.callback.S3Info { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

 -keep class com.xx.baseuilibrary.module.h5.interfaces.BaseRemoteJsInterface{*;}
 -keep class com.xx.xxmerchants.common.html.interfaces.JsNativeInterface{*;}

 -keep public class **.*beans*.** {
     public void set*(***);
     public *** get*();
     public *** is*();
 }

  -keep public class **.*bean*.** {
      public void set*(***);
      public *** get*();
      public *** is*();
  }

-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.webView, jav.lang.String);
}

# Keep JavascriptInterface
-keepclassmembers class ** {
    @android.webkit.JavascriptInterface public *;
}

 -keep class com.xx.baseuilibrary.module.h5.** { *; }
 -dontwarn com.xx.baseuilibrary.module.h5.**

 -dontwarn com.readystatesoftware.chuck.**
 -keep class com.readystatesoftware.chuck.**{*;}

 #share
 -dontwarn com.xx.share.**
 -keep class com.xx.share.**{*;}


 ##---------------神策  -------
 -dontwarn com.sensorsdata.analytics.android.**
 -keep class com.sensorsdata.analytics.android.** {
 *;
 }
 -keep class **.R$* {
     <fields>;
 }
 -keep class com.xx.xxmerchants.common.shence.** { *; }
 -keep public class * extends android.content.ContentProvider
 -keepnames class * extends android.view.View

 -keep class * extends android.app.Fragment {
  public void setUserVisibleHint(boolean);
  public void onHiddenChanged(boolean);
  public void onResume();
  public void onPause();
 }
 -keep class android.support.v4.app.Fragment {
  public void setUserVisibleHint(boolean);
  public void onHiddenChanged(boolean);
  public void onResume();
  public void onPause();
 }
 -keep class * extends android.support.v4.app.Fragment {
  public void setUserVisibleHint(boolean);
  public void onHiddenChanged(boolean);
  public void onResume();
  public void onPause();
 }

 -keep class com.crashlytics.** { *; }
 -dontwarn com.crashlytics.**
 -keepattributes *Annotation*
 -keepattributes SourceFile,LineNumberTable
 -keep public class * extends java.lang.Exception

  -keep class com.darsh.multipleimageselect.models.** { *; }