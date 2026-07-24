package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey val id: String,
    val subjectId: String,
    val subjectName: String,
    val subjectColorHex: String = "#6750A4",
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toSubjectColor(): Color {
        return try {
            val hex = subjectColorHex.removePrefix("#")
            val colorInt = if (hex.length == 6) {
                ("FF$hex").toLong(16).toInt()
            } else {
                hex.toLong(16).toInt()
            }
            Color(colorInt)
        } catch (e: Exception) {
            Color(0xFF6750A4)
        }
    }
}
