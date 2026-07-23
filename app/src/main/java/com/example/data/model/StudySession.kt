package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: Int,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionType: String = "Pomodoro", // "Pomodoro", "Manual", "Timer"
    val notes: String = ""
)
