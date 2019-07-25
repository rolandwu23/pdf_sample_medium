package com.eonreality.pdf

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var loadingDialog: MaterialDialog

    private var onDownloadComplte:BroadcastReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingDialog = getLoadingDialog("Downloading")

        val download = findViewById<Button>(R.id.download)
        download.setOnClickListener {
            if(!loadingDialog.isShowing) loadingDialog.show()
            downloadTest()
        }

    }

    fun downloadTest(){
        val url = "http://maven.apache.org/archives/maven-1.x/maven.pdf"
        val file = createFile("maven.pdf")
        val request = DownloadManager.Request(Uri.parse(url))
            .setDestinationUri(Uri.fromFile(file))// Uri of the destination file
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadID = downloadManager.enqueue(request)

        val uri =  FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file)

//        onDownloadComplte = object:BroadcastReceiver(){
//            override fun onReceive(context: Context?, intent: Intent?) {
//                val id= intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1)
//
//                if(loadingDialog.isShowing) loadingDialog.dismiss()
//                if(id == downloadID){
//                        Toast.makeText(this@MainActivity,"Download Complete",Toast.LENGTH_SHORT).show()
//                    openPdf(uri)
//                }
//            }
//        }
//
//        registerReceiver(onDownloadComplte,IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))


        var finishDownloading = false

        while (!finishDownloading){
            val cursor = downloadManager.query( DownloadManager.Query().setFilterById(downloadID))
            if(cursor.moveToFirst()){
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when(status){
                    DownloadManager.STATUS_FAILED -> {
                        Log.e("Status","Failed")
                        finishDownloading = true
                    }
                    DownloadManager.STATUS_PAUSED -> Log.e("Status","Paused")
                    DownloadManager.STATUS_PENDING ->  Log.e("Status","Pending")
                    DownloadManager.STATUS_RUNNING -> {
                        val total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if(total > 0){
                            val read = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
//                            runOnUiThread { loadingDialog.setProgress(Math.round(read * 100.0f / total))}
                            runOnUiThread { for(i in 1..100) loadingDialog.setProgress(i) }
                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        Log.e("Status","Successful")
                        finishDownloading = true
                        runOnUiThread { if(loadingDialog.isShowing) loadingDialog.dismiss() }
                        Toast.makeText(this@MainActivity,"Download Complete",Toast.LENGTH_SHORT).show()
                        openPdf(uri)
                    }
                }
            }

        }

    }

    fun createFile(fileName:String): File {

        val state = Environment.getExternalStorageState()
        val fileDirs = if(state == Environment.MEDIA_MOUNTED){
            File(Environment.getExternalStorageDirectory().toString() + "/PDF", "Document");
        } else {
            File(getExternalFilesDir(null),"Creator AVR Images")
        }
        if(!fileDirs.exists()) fileDirs.mkdirs()
        return File(fileDirs,fileName)
    }

    fun openPdf(uri:Uri){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.setDataAndType(uri, contentResolver.getType(uri))
        startActivity(Intent.createChooser(intent, "Open with:"))
    }

    fun getLoadingDialog(message: String): MaterialDialog {

        return MaterialDialog.Builder(this).apply{
            cancelable(false)
            typeface(Typeface.create("sans-serif-light", Typeface.BOLD), Typeface.create("sans-serif-light", Typeface.NORMAL))
            title(message).content("Please wait...")
            progress(false,100,true)
            backgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            widgetColor(ContextCompat.getColor(context, R.color.colorAccent))
        }.build()
    }

    override fun onDestroy() {
//        unregisterReceiver(onDownloadComplte)
        super.onDestroy()
    }
}




