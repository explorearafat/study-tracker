package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String, // e.g. "STEM", "Languages", "Humanities", "Arts", "Other"
    val colorHex: Long, // e.g. 0xFF4F46E5
    val targetDailyMinutes: Int = 60,
    val iconName: String = "Book" // e.g. "Book", "Calculator", "Science", "Computer", "Language", "Brush", "History"
)
