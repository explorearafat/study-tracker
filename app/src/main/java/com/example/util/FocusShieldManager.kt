package com.example.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object FocusShieldManager {
    var isShieldActive by mutableStateOf(false)
        private set

    var blockedAppsCount by mutableStateOf(4)

    fun toggleShield(context: Context, active: Boolean, subjectName: String = "Focus Session") {
        isShieldActive = active
        if (active) {
            com.example.notifications.FocusShieldService.start(context, subjectName, 1500)
        } else {
            com.example.notifications.FocusShieldService.stop(context)
        }
    }
}
