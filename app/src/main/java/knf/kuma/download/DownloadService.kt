package knf.kuma.download

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import knf.kuma.commons.PrefsUtil
import knf.kuma.commons.noCrash
import knf.kuma.commons.notNull
import knf.kuma.database.CacheDB
import knf.kuma.pojos.DownloadObject
import knf.kuma.queue.QueueManager
import knf.kuma.videoservers.ServersFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.util.concurrent.TimeUnit

class DownloadService : IntentService("Download service") {
    private val downloadsDAO = CacheDB.INSTANCE.downloadsDAO()

    private var manager: NotificationManager? = null

    private var current: DownloadObject? = null

    private var file: String? = null
    private val bufferSize = PrefsUtil.bufferSize()

    private val startNotification: Notification
        get() = NotificationCompat.Builder(this, CHANNEL_ONGOING)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(current!!.name)
                .setContentText(current!!.chapter)
                .setProgress(100, current!!.progress, true)
                .setOngoing(true)
                .setSound(null)
                .setWhen(current!!.time)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

    override fun onHandleIntent(intent: Intent?) {
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (intent == null)
            return
        current = downloadsDAO.getByEid(intent.getStringExtra("eid"))
        if (current == null)
            return
        file = current!!.file
        startForeground(DOWNLOADING_ID, startNotification)
        try {
            val request = Request.Builder()
                    .url(intent.dataString!!)
            if (current!!.headers != null)
                for (pair in current!!.headers!!.headers) {
                    request.addHeader(pair.first, pair.second)
                }
            val response = OkHttpClient().newBuilder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .followSslRedirects(true).build().newCall(request.build()).execute()
            current!!.t_bytes = response.body()!!.contentLength()
            val inputStream = BufferedInputStream(response.body()!!.byteStream())
            val outputStream: BufferedOutputStream
            if (response.code() == 200 || response.code() == 206) {
                outputStream = BufferedOutputStream(FileAccessHelper.INSTANCE.getOutputStream(current!!.file!!), bufferSize * 1024)
            } else {
                Log.e("Download error", "Code: " + response.code())
                errorNotification()
                downloadsDAO.delete(current!!)
                QueueManager.remove(current!!.eid!!)
                response.close()
                cancelForeground()
                return
            }
            current?.state = DownloadObject.DOWNLOADING
            downloadsDAO.update(current!!)
            val data = ByteArray(bufferSize * 1024)
            var count: Int = inputStream.read(data, 0, bufferSize * 1024)
            while (count >= 0) {
                val revised = downloadsDAO.getByEid(intent.getStringExtra("eid"))
                if (revised == null) {
                    FileAccessHelper.INSTANCE.delete(file)
                    downloadsDAO.delete(current!!)
                    QueueManager.remove(current?.eid)
                    cancelForeground()
                    return
                }
                outputStream.write(data, 0, count)
                current!!.d_bytes += count.toLong()
                val prog = (current!!.d_bytes * 100 / current!!.t_bytes).toInt()
                if (prog > current!!.progress) {
                    current!!.progress = prog
                    updateNotification()
                    downloadsDAO.update(current!!)
                }
                count = inputStream.read(data, 0, bufferSize * 1024)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            response.close()
            completedNotification()
        } catch (e: Exception) {
            e.printStackTrace()
            FileAccessHelper.INSTANCE.delete(file)
            downloadsDAO.delete(current!!)
            QueueManager.remove(current?.eid)
            errorNotification()
        }

    }

    private fun updateNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ONGOING)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(current!!.name)
                .setContentText(current!!.chapter)
                .setProgress(100, current!!.progress, false)
                .setOngoing(true)
                .setSound(null)
                .setWhen(current!!.time)
                .setPriority(NotificationCompat.PRIORITY_LOW)
        val pending = downloadsDAO.countPending()
        if (pending > 0)
            notification.setSubText(pending.toString() + " " + if (pending == 1) "pendiente" else "pendientes")
        manager!!.notify(DOWNLOADING_ID, notification.build())
    }

    private fun completedNotification() {
        current!!.state = DownloadObject.COMPLETED
        downloadsDAO.update(current!!)
        val notification = NotificationCompat.Builder(this, CHANNEL)
                .setColor(ContextCompat.getColor(applicationContext, android.R.color.holo_green_dark))
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(current!!.name)
                .setContentText(current!!.chapter)
                .setContentIntent(ServersFactory.getPlayIntent(this, current!!.name!!, file!!))
                .setOngoing(false)
                .setAutoCancel(true)
                .setWhen(current!!.time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        manager!!.notify(current!!.eid!!.toInt(), notification)
        updateMedia()
        cancelForeground()
    }

    private fun updateMedia() {
        try {
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(FileAccessHelper.INSTANCE.getFile(file!!))))
            MediaScannerConnection.scanFile(applicationContext, arrayOf(FileAccessHelper.INSTANCE.getFile(file!!).absolutePath), arrayOf("video/mp4"), null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun errorNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL)
                .setColor(ContextCompat.getColor(applicationContext, android.R.color.holo_red_dark))
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(current!!.name)
                .setContentText("Error al descargar " + current!!.chapter!!.toLowerCase())
                .setOngoing(false)
                .setWhen(current!!.time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        manager?.notify(current?.eid?.toInt() ?: 0, notification)
        cancelForeground()
    }

    private fun cancelForeground() {
        stopForeground(true)
        manager?.cancel(DOWNLOADING_ID)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        noCrash {
            cancelForeground()
            FileAccessHelper.INSTANCE.delete(file)
            if (current.notNull()) {
                if (manager.notNull())
                    errorNotification()
                downloadsDAO.delete(current!!)
                QueueManager.remove(current?.eid)
            }
        }
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        const val CHANNEL = "service.Downloads"
        const val CHANNEL_ONGOING = "service.Downloads.Ongoing"
        private const val DOWNLOADING_ID = 8879
    }
}
