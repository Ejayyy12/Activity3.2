package com.example.mywatertracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class WaterService : Service() {

    companion object {
        const val CHANNEL_ID = "water_channel"
        const val NOTIF_ID = 101
        const val EXTRA_ADD_WATER = "extra_add_water"
    }

    private var waterLevel = 0.0
    private val handler = Handler(Looper.getMainLooper())
    private val interval = 5000L

    private val decreaseTask = object : Runnable {
        override fun run() {
            waterLevel -= 0.144
            if (waterLevel < 0) waterLevel = 0.0
            updateNotification()
            handler.postDelayed(this, interval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notif = buildNotification()
        startForeground(NOTIF_ID, notif)

        handler.post(decreaseTask)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val add = intent?.getDoubleExtra(EXTRA_ADD_WATER, 0.0) ?: 0.0
        if (add > 0) {
            waterLevel += add
            updateNotification()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(decreaseTask)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("My Water Tracker")
            .setContentText("Water level: ${String.format("%.2f", waterLevel)} ml")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Water Tracker Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
