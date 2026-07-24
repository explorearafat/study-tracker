package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.Subject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificationHelper {
    const val CHANNEL_ID = "study_reminder_channel"
    const val CHANNEL_NAME = "Daily Study Reminders"
    const val GOAL_CHANNEL_ID = "study_goal_channel"
    const val GOAL_CHANNEL_NAME = "Subject Goal Alerts"
    const val NOTIFICATION_ID = 1001
    const val PENDING_INTENT_REQUEST_CODE = 2002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Daily reminders to stay consistent with your study goals."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(reminderChannel)

            val goalChannel = NotificationChannel(GOAL_CHANNEL_ID, GOAL_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alerts sent when you reach your subject's daily study goal."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(goalChannel)
        }
    }

    fun checkAndNotifySubjectGoal(context: Context, subject: Subject, totalTodaySeconds: Int): Boolean {
        if (subject.targetDailyMinutes <= 0) return false

        val todayMins = totalTodaySeconds / 60
        if (todayMins >= subject.targetDailyMinutes) {
            val prefs = context.getSharedPreferences("study_goal_prefs", Context.MODE_PRIVATE)
            val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val key = "goal_notified_${subject.id}_$todayDateStr"

            val alreadyNotified = prefs.getBoolean(key, false)
            if (!alreadyNotified) {
                showGoalReachedNotification(
                    context = context,
                    subjectId = subject.id,
                    subjectName = subject.name,
                    targetMins = subject.targetDailyMinutes,
                    totalStudiedMins = todayMins
                )
                prefs.edit().putBoolean(key, true).apply()
                return true
            }
        }
        return false
    }

    fun showGoalReachedNotification(
        context: Context,
        subjectId: Int,
        subjectName: String,
        targetMins: Int,
        totalStudiedMins: Int
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentPendingIntent = PendingIntent.getActivity(context, subjectId, intent, flags)

        val title = "🎉 Daily Goal Reached for $subjectName!"
        val message = "Awesome effort! You completed your target of $targetMins mins today ($totalStudiedMins mins logged)."

        val builder = NotificationCompat.Builder(context, GOAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(3000 + subjectId, builder.build())
    }

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        createNotificationChannel(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = StudyReminderReceiver.ACTION_STUDY_REMINDER
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            PENDING_INTENT_REQUEST_CODE,
            intent,
            flags
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = StudyReminderReceiver.ACTION_STUDY_REMINDER
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            PENDING_INTENT_REQUEST_CODE,
            intent,
            flags
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun showStudyNotification(context: Context, title: String, message: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentPendingIntent = PendingIntent.getActivity(context, 0, intent, flags)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
