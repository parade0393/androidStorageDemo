<div align="center"><img width="70%" src="https://img-blog.csdnimg.cn/20200421230158859.png"/></div>

内部存储，我们称为InternalStorage，外部存储我们称为ExternalStorage。注意内部存储不是内存。从用户角度来说SD卡有内置和外置之分，但是对于开发者，只有内部存储和外部存储，内置SD卡和外置SD卡都属于外部存储范畴,打开AndroidStudio的FileExceplorer,其中data是指内部存储，

一般mnt是外部存储，mnt有一个sdcard文件夹，这个文件夹中的文件又分为两类，一类是公有目录，还有一类是私有目录，其中的公有目录有九大类，比如DCIM、DOWNLOAD等这种系统为我们创建的文件夹，私有目录就是**Android这个文件夹**，这个文件夹打开之后里边有一个**data文件夹**，打开这个data文件夹，里边有**许多包名组成的文件夹**

<div align="center"><img width="70%" height="400px" src="https://img-blog.csdnimg.cn/20200421172731710.png"/></div>

------

外部存储操作，首先申请权限

```kotlin
val externalStorageDirectory = Environment.getExternalStorageDirectory().absolutePath
val file = File("$externalStorageDirectory/parade/a.txt")
if(!file.exists()) file.parentFile.mkdir()//在外部存储创建文件夹
Log.e(TAG,"external-root:${externalStorageDirectory}")//external-root:/storage/emulated/0
val filePath = getExternalFilesDir(null)?.absolutePath
//external-file-path:/storage/emulated/0/Android/data/com.parade.storagedemo/files
Log.e(TAG,"external-file-path:${filePath}")//外部存储应用私有目录
val privateFile = File("${getExternalFilesDir(null)?.parentFile}/jerry")
        if (!privateFile.exists()) privateFile.mkdir()//在外部存储应用私有目录根目录创建文件夹
val cachePath = externalCacheDir?.absolutePath
///storage/emulated/0/Android/data/com.parade.storagedemo/cache外部存储应用私有目录
Log.e(TAG,"external-file-path:${cachePath}")
```

下载网络资源存储本地

```kotlin
private fun download() {
    thread {
       try {
           val url =
               URL("https://cdn.pixabay.com/photo/2020/04/02/11/20/fungus-4994622_960_720.jpg")
           //打开链接
           val openConnection = url.openConnection() as HttpURLConnection
           val picFile = File("${ getExternalFilesDir(null)?.parentFile}/pic/12345.jpg")
           //创建文件夹,不需要creatNewFile
           if (!picFile.parentFile.exists()) picFile.parentFile.mkdir()
           openConnection.inputStream.use {input->
               BufferedOutputStream(FileOutputStream(picFile)).use {output->
                   input.copyTo(output)
               }
           }
       }catch (e:Exception){
           e.printStackTrace()
       }
    }
}
```

```kotlin
    fun saveFile() {
        if (checkPermission()) {
            //getExternalStoragePublicDirectory被弃用，分区存储开启后就不允许访问了
            val filePath = Environment.getExternalStoragePublicDirectory("").toString() + "/test3.txt"
            val fw = FileWriter(filePath)
            fw.write("hello world")
            fw.close()
            showToast("文件写入成功")
        }
    }
```
### 针对上述代码分情况运行：
1） targetSdkVersion = 28，运行后正常读写。
2） targetSdkVersion = 29，不删除应用，targetSdkVersion 由28修改到29，覆盖安装，运行后正常读写。
3） targetSdkVersion = 29，删除应用，重新运行，读写报错，程序崩溃（open failed: EACCES (Permission denied)）
4） targetSdkVersion = 29，添加android:requestLegacyExternalStorage="true"（不启用分区存储），删除应用，重新运行,读写正常不报错
5） targetSdkVersion = 30，不删除应用，targetSdkVersion 由29修改到30，读写报错，程序崩溃（open failed: EACCES (Permission denied)）
6） targetSdkVersion = 30，不删除应用，targetSdkVersion 由29修改到30，增加android:preserveLegacyExternalStorage="true"，读写正常不报错
7） targetSdkVersion = 30，删除应用，重新运行，读写报错，程序崩溃（open failed: EACCES (Permission denied)）

### Android Q规定了APP有两种外部存储空间视图模式：Legacy View、Filtered View。
    * Legacy View 兼容模式。与AndroidQ之前一样，申请权限后App可访问外部存储，拥有完整的访问权限，可以使用File的方式访问文件
    * Filtered View 分区存储。APP只能直接访问App-specific目录，访问公共目录或者其他APP的App-specific目录，只能通过MediaStore、SAF、或者其他APP提供的ContentProvider、FileProvider等方式访问
    * 在AndroidQ上，target SDK大于或等于29的APP默认被赋予Filtered View。APP可以在AndroidManifest.xml中设置requestLegacyExternalStorage来修改外部存储空间视图模式，true为Legacy View，false为Filtered View
    * 在Android 11以上，强制是Filtered View
    * 可以通过Environment.isExternalStorageLegacy()方法判断运行模式

### 外部存储空间被分为两部分
* App-specific directory 沙盒目录
    * APP只能在Context.getExternalFilesDir()目录下通过File的方式创建文件，APP卸载的时候，这个目录下的文件会被删除
    * 其他路径下无法通过File的方式创建文件----无需任何权限
* 2.Public Directory 公共目录，处理文件不能用file，必须用Uri
    * 公共目录包括：多媒体公共目录（photos, images, videos, audio）和下载文件目录（Downloads）
    * APP通过MediaStore或者SAF（System Access Framework）的方式访问其中的文件
    * APP卸载后，文件不会被删除
    * 通过MediaStore访问其他应用创建的多媒体文件的时候，需要READ_EXTERNAL_STORAGE权限

对于androidQ 来说，没有filePath这个东西了，只有自己沙盒中的文件才能用filepath，其他地方只能用uri,所以文件管理器上传文件需要处理兼容10以上
对于androidQ 来说，没有WRITE_EXTERNAL_STORAGE权限
对于androidQ 以后来说，要使用ContentResolver来读取和保存文件

