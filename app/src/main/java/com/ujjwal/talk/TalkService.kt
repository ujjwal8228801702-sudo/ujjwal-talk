package com.ujjwal.talk

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class TalkService : Service() {
    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel("ujjwal_talk", "Ujjwal Talk", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val notification: Notification = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, "ujjwal_talk")
                .setContentTitle("Ujjwal Talk connected")
                .setContentText("आपका वॉकी-टॉकी ग्रुप बैकग्राउंड में चल रहा है")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true).build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Ujjwal Talk connected")
                .setContentText("ग्रुप बैकग्राउंड में चल रहा है")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true).build()
        }
        startForeground(101, notification)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
}
