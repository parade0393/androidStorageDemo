package com.parade.storagedemo

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

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

        button9.setOnClickListener {
            download()
        }

        button10.setOnClickListener {
            android10Download()
        }

    }

    /** android10需要先创建一个URI,而创建URI需要ContentValues */
    private fun android10Download() {
        thread {
            try {
                val url =
                    URL("https://cdn.pixabay.com/photo/2020/04/02/11/20/fungus-4994622_960_720.jpg")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                val values = ContentValues()
                //创建文件名
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, "12345.jpg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //创建文件路径
                   /* values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/${packageName}/a"
                    )*/
                //这个api在Android10之前没有 MediaStore.MediaColumns.RELATIVE_PATH
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH,"${getExternalFilesDir(null)}/download")
                    //创建文件URI (MediaStore.Downloads在Android10之前也没有
                    val uri =
                        contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    connection.inputStream.use { input ->
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
            }
        }
    }

    private fun download() {
        thread {
            try {
                val url =
                    URL("https://cdn.pixabay.com/photo/2020/04/02/11/20/fungus-4994622_960_720.jpg")
                val conn = url.openConnection() as HttpURLConnection
                val picFile = File("${getExternalFilesDir(null)?.parentFile}/parade/parade.jpg")
                picFile.parentFile?.let {
                    if (!it.exists()) it.mkdir()
                }
                conn.inputStream.use { intput ->
                    BufferedOutputStream(FileOutputStream(picFile)).use { output ->
                        intput.copyTo(output)
                    }
                }
            } catch (e: Exception) {

            }
        }
    }
}
