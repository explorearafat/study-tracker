package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val targetMinutesPerWeek: Int = 300,
    val iconName: String = "book"
) {
    fun toColor(): Color {
        return try {
            val hex = colorHex.removePrefix("#")
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

    companion object {
        val DEFAULT_PALETTE = listOf(
            "#4CAF50" to "Emerald Green",
            "#2196F3" to "Ocean Blue",
            "#9C27B0" to "Royal Purple",
            "#FF9800" to "Sunset Orange",
            "#E91E63" to "Vibrant Pink",
            "#00BCD4" to "Bright Teal",
            "#FF5722" to "Warm Red",
            "#795548" to "Deep Brown",
            "#3F51B5" to "Indigo",
            "#009688" to "Jade Green"
        )
    }
}
