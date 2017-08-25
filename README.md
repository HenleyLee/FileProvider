# FileProvider-master —— Android 7.0 FileProvider适配

## 1.介绍 ##
对于Android 7.0，提供了非常多的变化，详细的可以阅读官方文档Android 7.0 行为变更，记得当时做了多窗口支持、FileProvider以及7.1的3D Touch的支持，不过和我们开发者关联最大的，或者说必须要适配的就是去除项目中传递file://类似格式的uri了。
在官方7.0的以上的系统中，尝试传递 file://URI可能会触发FileUriExposedException。
对于面向 Android 7.0 的应用，Android 框架执行的 StrictMode API 政策禁止在您的应用外部公开 file:// URI。如果一项包含文件 URI 的 intent 离开您的应用，则应用出现故障，并出现 FileUriExposedException 异常。
要在应用间共享文件，您应发送一项 content:// URI，并授予 URI 临时访问权限。进行此授权的最简单方式是使用 FileProvider 类。如需了解有关权限和共享文件的详细信息，请参阅共享文件。
https://developer.android.com/about/versions/nougat/android-7.0-changes.html#accessibility
那么下面就看看如何通过FileProvider解决此问题吧。

## 2.使用FileProvider的步骤： ##
其实对于如何使用FileProvider，其实在FileProvider的API页面也有详细的步骤，有兴趣的可以看下。
    https://developer.android.com/reference/android/support/v4/content/FileProvider.html
FileProvider实际上是ContentProvider的一个子类，它的作用也比较明显了，file:///Uri不给用，那么换个Uri为content://来替代。
下面我们看下整体的实现步骤，并考虑为什么需要怎么做？

#### 1.声明provider ####
定义一个authorities，一般定义为“${applicationId}.fileprovider”
```xml
    <provider
        android:name="android.support.v4.content.FileProvider"
        android:authorities="${applicationId}.fileprovider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>
```
* android:name --- 定义FileProvider路径名称
* android:authorities --- 定义authority
* android:exported="false" --- 对其它应用不可用
* android:grantUriPermissions="true"  既然对其它应用不可用，只能授予content uri临时权限
* `<meta-data>`中的android:name="android.support.FILE_PROVIDER_PATHS"(这个名子是固定的)和android:resource 指向一个xml文件，这个xml文件定义了要共享文件的路径

注意一点，他需要设置一个meta-data，里面指向一个xml文件。

#### 2.在res目录下新建目录xml，在xml目录下新建file_paths.xml文件： ####
```xml
    <?xml version="1.0" encoding="utf-8"?>
    <paths xmlns:android="http://schemas.android.com/apk/res/android">
        <root-path name="root" path="" />
        <files-path name="files" path="path" />
        <cache-path name="cache" path="path" />
        <external-path name="external" path="path" />
        <external-files-path name="external_files" path="path" />
        <external-cache-path name="external_cache" path="path" />
    </paths>
```
在paths节点内部支持以下几个子节点，分别为：
* `<root-path name="root" path="" /\>` 表示设备的根目录，即 new File("/")
* `<files-path name="files" path="path" />` 表示应用程序内部存储区域的文件子目录中的文件，即 Context.getFilesDir()
* `<cache-path name="cache" path="path" />` 表示应用程序内部存储区域的缓存子目录中的文件，即 Context.getCacheDir()
* `<external-path name="external" path="path" />` 表示外部存储区根目录中的文件，即 Environment.getExternalStorageDirectory()
* `<external-files-path name="external_files" path="path" />` 表示应用程序外部存储区域的文件子目录中的文件，即 Context#getExternalFilesDir(String)
* `<external-cache-path name="external_cache" path="path" />` 表示应用程序外部存储区域的缓存子目录中的文件，即 Context#getExternalCacheDir()

每个节点都支持两个属性：
* `name` URI路径段。为了加强安全性，这个值隐藏了共享的子目录的名称。此值代表的子目录名称包含在路径属性中。
* `path` 共享的实际子目录名称。值得注意的是，该值指的是子目录，而不是文件。不能共享一个文件的文件名，也可以指定使用通配符的文件子集。
当这么声明以后，代码可以使用你所声明的当前文件夹以及其子文件夹。

## 3.使用FileProvider API并进行授权： ##

```java
    Uri fileUri = null;
    Intent intent = new Intent();
    if (SDKVersionHelper.isOverNougat()) {
        fileUri = FileProvider.getUriForFile(this, AUTHORITY, file);// 通过FileProvider创建一个content类型的Uri
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 表示对目标应用临时授权该Uri所代表的文件
    } else {
        fileUri = Uri.fromFile(file);
    }
```