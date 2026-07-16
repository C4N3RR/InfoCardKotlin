package com.example.infocard.data

import androidx.compose.ui.graphics.Color

enum class CardTheme(val hexList: List<String>, val displayName: String) {
    PURPLE_PINK(listOf("6D28D9", "DB2777"), "Neon Nebula"),
    BLUE_TEAL(listOf("028090", "00C6FF"), "Ocean Breeze"),
    ORANGE_RED(listOf("FF416C", "FF4B2B"), "Sunset Flame"),
    MIDNIGHT_GOLD(listOf("1F2937", "111827"), "Obsidian Gold"),
    EMERALD_MINT(listOf("059669", "028090"), "Emerald Mint");

    val colors: List<Color>
        get() = hexList.map { hexToColor(it) }

    companion object {
        fun hexToColor(hex: String): Color {
            val colorInt = hex.trimStart('#').toLong(16)
            return if (hex.length == 6) {
                Color(colorInt or 0xFF000000)
            } else {
                Color(colorInt)
            }
        }
    }
}
