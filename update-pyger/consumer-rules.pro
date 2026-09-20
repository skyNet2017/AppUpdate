# 宿主开启 minify 后仍需通过反射读取应用 module 的 BuildConfig 字段
-keep class **.BuildConfig { *; }
-keepclassmembers class **.BuildConfig {
    public static <fields>;
}
