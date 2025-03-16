package com.example.cleanlauncher

data class AppInfo(
    val name: String,
    val packageName: String,
    var state: AppState = AppState.NEITHER,
    var customName: String? = null
) {
    fun displayName(): String {
        return customName ?: name
    }
}

enum class AppState {
    FAVORITE,
    HIDDEN,
    NEITHER,
    BAD
}