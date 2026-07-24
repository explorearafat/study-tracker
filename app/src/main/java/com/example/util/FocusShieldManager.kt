package com.example.util

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings

data class SocialAppItem(
    val id: String,
    val displayName: String,
    val iconName: String,
    val packageNames: List<String>
)

object FocusShieldManager {

    private const val PREFS_NAME = "focus_shield_prefs"
    private const val KEY_SHIELD_ENABLED = "key_shield_enabled"
    private const val KEY_BLOCKED_APPS = "key_blocked_apps"
    private const val KEY_DND_ENABLED = "key_dnd_enabled"

    val AVAILABLE_SOCIAL_APPS = listOf(
        SocialAppItem("facebook", "Facebook", "facebook", listOf("com.facebook.katana")),
        SocialAppItem("messenger", "Messenger", "messenger", listOf("com.facebook.orca", "com.facebook.mlite")),
        SocialAppItem("instagram", "Instagram", "instagram", listOf("com.instagram.android")),
        SocialAppItem("tiktok", "TikTok", "tiktok", listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill", "com.zhiliaoapp.musically.go")),
        SocialAppItem("whatsapp", "WhatsApp", "whatsapp", listOf("com.whatsapp", "com.whatsapp.w4b")),
        SocialAppItem("youtube", "YouTube", "youtube", listOf("com.google.android.youtube")),
        SocialAppItem("twitter", "Twitter / X", "twitter", listOf("com.twitter.android")),
        SocialAppItem("snapchat", "Snapchat", "snapchat", listOf("com.snapchat.android"))
    )

    val DEFAULT_BLOCKED_IDS = setOf("facebook", "messenger", "instagram", "tiktok", "whatsapp")

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isShieldEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHIELD_ENABLED, true)
    }

    fun setShieldEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SHIELD_ENABLED, enabled).apply()
    }

    fun isDndEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DND_ENABLED, true)
    }

    fun setDndEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DND_ENABLED, enabled).apply()
    }

    fun getBlockedAppIds(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_BLOCKED_APPS, DEFAULT_BLOCKED_IDS) ?: DEFAULT_BLOCKED_IDS
    }

    fun setBlockedAppIds(context: Context, appIds: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_BLOCKED_APPS, appIds).apply()
    }

    fun isAppBlocked(context: Context, appId: String): Boolean {
        return getBlockedAppIds(context).contains(appId)
    }

    fun toggleAppBlocked(context: Context, appId: String) {
        val current = getBlockedAppIds(context).toMutableSet()
        if (current.contains(appId)) {
            current.remove(appId)
        } else {
            current.add(appId)
        }
        setBlockedAppIds(context, current)
    }

    fun getBlockedPackageNames(context: Context): Set<String> {
        val blockedIds = getBlockedAppIds(context)
        return AVAILABLE_SOCIAL_APPS
            .filter { blockedIds.contains(it.id) }
            .flatMap { it.packageNames }
            .toSet()
    }

    fun getAppNameForPackage(packageName: String): String {
        return AVAILABLE_SOCIAL_APPS.find { app -> app.packageNames.contains(packageName) }?.displayName
            ?: "Social Media App"
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageStatsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                openUsageStatsSettings(context)
            }
        }
    }

    fun hasDndPermission(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    fun openDndSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                openUsageStatsSettings(context)
            }
        }
    }

    fun applyDndMode(context: Context, enable: Boolean) {
        if (!isDndEnabled(context)) return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager.isNotificationPolicyAccessGranted) {
            try {
                if (enable) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                } else {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getForegroundPackageName(context: Context): String? {
        if (!hasUsageStatsPermission(context)) return null
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 3000

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var currentForegroundPackage: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentForegroundPackage = event.packageName
            }
        }

        return currentForegroundPackage
    }
}
