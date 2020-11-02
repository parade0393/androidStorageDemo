package com.parade.storagedemo

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files.createFile
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val TAG = "MainActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //手机正在运行的版本号大于6.0->申请权限
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }

        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || Environment.MEDIA_MOUNTED_READ_ONLY == Environment.getExternalStorageState()) {
            Log.e(TAG, "ExternalStorage  state true")
        } else {
            Log.e(TAG, "ExternalStorage  state false")
        }

        //外部存储根目录
        val externalStorageDirectory = Environment.getExternalStorageDirectory().absolutePath
        val file = File("$externalStorageDirectory/parade/a.txt")
        if (!file.exists()) file.parentFile?.mkdir()
        Log.e(TAG, "external-root:${externalStorageDirectory}")
        val filePath = getExternalFilesDir(null)?.absolutePath
        val privateFile = File("${getExternalFilesDir(null)?.parentFile}/jerry")
        if (!privateFile.exists()) privateFile.mkdir()
        Log.e(TAG, "external-file-path:${filePath}")
        val cachePath = externalCacheDir?.absolutePath
        Log.e(TAG, "external-file-path:${cachePath}")

        button9.setOnClickListener(this)

        button10.setOnClickListener(this)

        upload.setOnClickListener(this)

    }

    /**
     * android10 以后分为沙河目录和公共目录 沙盒目录可直接通过File操作，公共目录则的通过MediaStore或者saf
     * android10需要先创建一个URI,而创建URI需要ContentValues */
    private fun android10Download() {
        thread {
            try {
                val url =
                    URL("https://img-blog.csdnimg.cn/20200520211751565.jpeg")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues()
                    //创建文件名
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, "12345.jpeg")
                    //创建共享文件路径
                   /* values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/${packageName}/a"
                    )*/
                //创建私有文件路径这个api在Android10之前没有 MediaStore.MediaColumns.RELATIVE_PATH
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH,"${getExternalFilesDir(null)}/download")
                    //创建文件URI (MediaStore.Downloads在Android10之前也没有
                    val uri =
                            contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    connection.inputStream.use { input ->//use扩展函数会自动关闭流
                        uri?.let { uri ->
                            contentResolver.openOutputStream(uri).use { output ->
                                output?.let {
                                    input.copyTo(it)
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, e.message?:"10")
            }
        }
    }

    private fun download() {
        thread {
            try {
                val url =
                    URL("https://img-blog.csdnimg.cn/20200520211751565.jpeg")
                val conn = url.openConnection() as HttpURLConnection
                val sPath = File(getExternalFilesDir(null),"pic")//创建文件1
//                val picFile = File("$sPath/pic.jpeg")//方法1//android/data/packgename/files/pic

                //创建文件2
                val picFile = File("${getExternalFilesDir(null)}/parade/parade.jpeg")//android/data/packgename/files/parade
                //创建文件3
                val filepath = getExternalFilesDir("dir")?.absolutePath
                val newFile = File("${filepath}${File.separator}file.jpeg")

                newFile.let {
                    newFile.parentFile?.let {pF->
                       if (!pF.exists()) pF.mkdirs()
                   }
                    if (it.exists()) it.delete()
                }
                conn.inputStream.use { intput ->
                    BufferedOutputStream(FileOutputStream(newFile)).use { output ->
                        intput.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button9-> download()
            R.id.button10 -> android10Download()
            R.id.upload -> upload()
        }
    }

    private fun createFile(){

    }

    private fun upload(){
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            val arrayOf = arrayOf("images/*", "application/pdf")
            putExtra(Intent.EXTRA_MIME_TYPES,arrayOf)
        }.also {
            startActivityForResult(it,2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.data
        Log.d(TAG, "onActivityResult: uri:$uri")
        if (uri?.scheme == ContentResolver.SCHEME_FILE){
            Log.d(TAG, "onActivityResult: SCHEME_FILE")
        }else if (uri?.scheme == ContentResolver.SCHEME_CONTENT){
            Log.d(TAG, "onActivityResult: SCHEME_CONTENT")
        }else{
            Log.d(TAG, "onActivityResult: ${uri?.scheme}")
        }
    }
}
