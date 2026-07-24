package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.util.FocusShieldManager
import kotlinx.coroutines.*

class FocusShieldService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitorJob: Job? = null

    companion object {
        const val CHANNEL_ID = "focus_shield_channel"
        const val NOTIFICATION_ID = 8881
        const val ACTION_START_SHIELD = "com.example.action.START_FOCUS_SHIELD"
        const val ACTION_STOP_SHIELD = "com.example.action.STOP_FOCUS_SHIELD"
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"

        var isRunning = false
            private set

        fun startService(context: Context, subjectName: String, remainingSeconds: Int) {
            val intent = Intent(context, FocusShieldService::class.java).apply {
                action = ACTION_START_SHIELD
                putExtra(EXTRA_SUBJECT_NAME, subjectName)
                putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FocusShieldService::class.java).apply {
                action = ACTION_STOP_SHIELD
            }
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_SHIELD) {
            stopShield()
            stopSelf()
            return START_NOT_STICKY
        }

        val subjectName = intent?.getStringExtra(EXTRA_SUBJECT_NAME) ?: "Focus Session"
        val remainingSeconds = intent?.getIntExtra(EXTRA_REMAINING_SECONDS, 1500) ?: 1500

        startForegroundServiceInternal(subjectName, remainingSeconds)
        startMonitoring(subjectName)

        return START_STICKY
    }

    private fun startForegroundServiceInternal(subjectName: String, remainingSeconds: Int) {
        isRunning = true
        FocusShieldManager.applyDndMode(this, true)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val minutes = remainingSeconds / 60
        val notificationContent = "🛡️ Focus Shield Active for $subjectName (${minutes}m left) | Social Apps Blocked & DND On"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Shield Guard Active")
            .setContentText(notificationContent)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startMonitoring(subjectName: String) {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            var lastBlockedAlertTime = 0L

            while (isActive) {
                try {
                    val context = applicationContext
                    if (FocusShieldManager.isShieldEnabled(context)) {
                        val blockedPackages = FocusShieldManager.getBlockedPackageNames(context)
                        val currentPackage = FocusShieldManager.getForegroundPackageName(context)

                        if (currentPackage != null && currentPackage != packageName && blockedPackages.contains(currentPackage)) {
                            val now = System.currentTimeMillis()
                            if (now - lastBlockedAlertTime > 2500) {
                                lastBlockedAlertTime = now
                                val blockedAppName = FocusShieldManager.getAppNameForPackage(currentPackage)
                                triggerBlockAlert(blockedAppName)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1200)
            }
        }
    }

    private fun triggerBlockAlert(blockedAppName: String) {
        val alertIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("SHOW_FOCUS_BLOCK_ALERT", true)
            putExtra("BLOCKED_APP_NAME", blockedAppName)
        }
        startActivity(alertIntent)
    }

    private fun stopShield() {
        isRunning = false
        monitorJob?.cancel()
        FocusShieldManager.applyDndMode(this, false)
        stopForeground(true)
    }

    override fun onDestroy() {
        stopShield()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Shield Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active App Blocker and DND Guard status during focus sessions"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
