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

项目的

