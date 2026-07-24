package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Student Scholar",
    val academicLevel: String = "University Undergraduate",
    val motto: String = "Building consistent habits every single day.",
    val targetDailyHours: Float = 3.0f,
    val avatarUri: String = "",
    val isDarkMode: Boolean = false,
    val pomodoroWorkMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5,
    val pomodoroLongBreakMinutes: Int = 15,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val isOnboardingCompleted: Boolean = false
)
