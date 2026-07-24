package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class FocusShieldService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val subjectName = intent?.getStringExtra("SUBJECT_NAME") ?: "Focus Session"
        val remainingSeconds = intent?.getIntExtra("REMAINING_SECONDS", 1500) ?: 1500
        val minutes = remainingSeconds / 60

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🛡️ Focus Shield Active")
            .setContentText("Focusing on $subjectName (${minutes}m remaining) | Social apps blocked & DND ON")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1001, notification)
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Shield Guard",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active Focus Shield status"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "focus_shield_channel"

        fun start(context: Context, subjectName: String, remainingSeconds: Int) {
            val intent = Intent(context, FocusShieldService::class.java).apply {
                putExtra("SUBJECT_NAME", subjectName)
                putExtra("REMAINING_SECONDS", remainingSeconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusShieldService::class.java)
            context.stopService(intent)
        }
    }
}
