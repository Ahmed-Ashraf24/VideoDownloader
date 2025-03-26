package com.example.videodownloader

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.logging.Handler
import kotlin.concurrent.thread
import kotlin.math.round

class DownloadService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_CANCEL = "ACTION_CANCEL"
        const val CHANNEL_ID = "download_channel"
    }

    private var counter = 0
    private var FILE_URL = ""
    private val FILE_NAME = "Video.mp4"
    private lateinit var file: File
    private var downloadedBytes: Long = 0L
    private var isDownloading = false
    private var isPaused = false
    private var totalVideoSizeInMB: Double = 0.0
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.d("checking is the service created", "the service is created ")
        file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            FILE_NAME
        )

        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("checking is the service strated", "the service is started ")
        FILE_URL=intent!!.getStringExtra("VideoURL")!!
        val handler = android.os.Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                counter = counter.inc()
                handler.postDelayed(this, 1000)
                println("counter :" + counter)
            }

        }
        handler.post(runnable)

        when (intent?.action) {

            ACTION_START -> {
                Log.d("checking if start action is passed", "start action is passed ")

                isPaused = false
                startDownload()
            }

            ACTION_PAUSE -> {
                isPaused = true
                updateNotification("Paused", -1)
            }

            ACTION_RESUME -> {
                isPaused = false
                startDownload()
            }

            ACTION_CANCEL -> {
                cancelDownload()
            }
        }
        return START_STICKY
    }

    private fun startDownload() {
        if (isDownloading) return
        isDownloading = true

        thread {
            try {
                val client = OkHttpClient()

                if (file.exists()) {
                    downloadedBytes = file.length()
                }

                val request = Request.Builder()
                    .url(FILE_URL)
                    .apply {
                        if (downloadedBytes > 0) {
                            addHeader("Range", "bytes=$downloadedBytes-")
                            //addHeader("User-Agent", "Mozilla/5.0")
                            //addHeader("Accept", "*/*")
                        }
                    }
                    .build()

                val response = client.newCall(request).execute()
                val fileSize = response.body?.contentLength()?.plus(downloadedBytes) ?: 0
                if (file.exists() && file.length() == fileSize) {
                    Toast.makeText(this, "this file is already downloaded ", Toast.LENGTH_SHORT)
                        .show()
                    isDownloading = false
                    return@thread
                }
                totalVideoSizeInMB = (fileSize.toDouble() / (1024 * 1024))
                if (response.code == 206 || response.code == 200) {
                    val inputStream: InputStream? = response.body?.byteStream()
                    val outputStream = FileOutputStream(file, true)

                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalDownloaded = downloadedBytes
                    var internalcounter = counter
                    var lastDownloadedSize = 0L

                    while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                        if (isPaused) break
                        outputStream.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead
                        val progress = ((totalDownloaded * 100) / fileSize).toInt()
                        val downloadedInMB = (totalDownloaded.toDouble() / (1024 * 1024))
                        Log.d("Download", "Downloaded: $totalDownloaded bytes")
                        if (internalcounter == 0) {
                            val speed = (totalDownloaded - lastDownloadedSize).toDouble()
                            lastDownloadedSize = totalDownloaded


                            updateNotification(
                                "Downloading: ${"%.2f".format(downloadedInMB)} / ${
                                    "%.2f".format(
                                        totalVideoSizeInMB
                                    )
                                } \n time remaining : ${"%.02f".format((speed / 1024) / downloadedInMB)} sec ",
                                progress
                            )

                        }

                        if (internalcounter < counter) {
                            val speed = (totalDownloaded - lastDownloadedSize).toDouble()
                            lastDownloadedSize = totalDownloaded
                            updateNotification(
                                "Downloading: ${"%.2f".format(downloadedInMB)} / ${
                                    "%.2f".format(
                                        totalVideoSizeInMB
                                    )
                                } \n time remaining : ${"%.02f".format((speed / 1024) / downloadedInMB)} sec",
                                progress
                            )

                            internalcounter = counter
                        }
                    }

                    outputStream.flush()
                    outputStream.close()
                    inputStream?.close()

                    if (!isPaused) {
                        updateNotification("Download Completed", 100)
                        stopSelf()
                    }

                } else {
                    Log.e("Download", "Server does not support resume (HTTP ${response.code})")
                }

            } catch (e: Exception) {
                Log.e("Download", "Error: ${e.message}")
                Toast.makeText(this,"something went wrong check the storage or the link",Toast.LENGTH_SHORT).show()
            } finally {
                isDownloading = false
            }
        }
    }

    private fun cancelDownload() {
        if (file.exists()) {
            file.delete()
        }
        stopSelf()
        notificationManager.cancel(1)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Download Progress",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(content: String, progress: Int) {

        val pauseIntent = Intent(this, DownloadService::class.java).apply {
            action = ACTION_PAUSE
        }
        val resumeIntent = Intent(this, DownloadService::class.java).apply {
            action = ACTION_RESUME
        }
        val cancelIntent = Intent(this, DownloadService::class.java).apply {
            action = ACTION_CANCEL
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("File Download")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Pause",
                PendingIntent.getService(
                    this,
                    0,
                    pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                android.R.drawable.ic_media_play,
                "Resume",
                PendingIntent.getService(
                    this,
                    1,
                    resumeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                android.R.drawable.ic_delete,
                "Cancel",
                PendingIntent.getService(
                    this,
                    2,
                    cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setOngoing(progress in 0..99)
            .build()


        notificationManager.notify(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
