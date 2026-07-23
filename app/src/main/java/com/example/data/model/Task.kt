package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: Int? = null,
    val title: String,
    val description: String = "",
    val dueDateEpochMs: Long? = null,
    val priority: String = "Medium", // "High", "Medium", "Low"
    val isCompleted: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
