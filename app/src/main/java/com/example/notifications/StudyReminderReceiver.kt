package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StudyReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_STUDY_REMINDER = "com.example.action.STUDY_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Optional boot completed handling
        } else if (action == ACTION_STUDY_REMINDER || action == null) {
            val title = "Daily Study Reminder! 📚"
            val message = "Stay consistent with your learning goals. Open the app to start your study session today!"
            NotificationHelper.showStudyNotification(context, title, message)
        }
    }
}
