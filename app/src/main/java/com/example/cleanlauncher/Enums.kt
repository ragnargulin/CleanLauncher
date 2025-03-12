// File: Enums.kt
package com.example.cleanlauncher

enum class AppState {
    FAVORITE,
    HIDDEN,
    NEITHER
}

enum class FontSize(val textSize: Float) {
    SMALL(20f),
    MEDIUM(30f),
    LARGE(40f),
    XLARGE(50f)
}

enum class AppNameTextStyle {
    ALL_LOWERCASE,
    LEADING_UPPERCASE
}