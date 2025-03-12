package com.example.cleanlauncher

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class LauncherPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "launcher_prefs", Context.MODE_PRIVATE
    )

    fun isStatusBarVisible(): Boolean {
        return prefs.getBoolean(KEY_STATUS_BAR_VISIBLE, true)
    }

    fun setStatusBarVisible(isVisible: Boolean) {
        prefs.edit { putBoolean(KEY_STATUS_BAR_VISIBLE, isVisible) }
    }

    fun addFavorite(packageName: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(packageName)
        prefs.edit(commit = true) { putStringSet(KEY_FAVORITES, favorites) }
    }

    fun removeFavorite(packageName: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(packageName)
        prefs.edit(commit = true) { putStringSet(KEY_FAVORITES, favorites) }
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun hideApp(packageName: String) {
        removeFavorite(packageName)  // Ensure it's not a favorite
        val hiddenApps = getHiddenApps().toMutableSet()
        hiddenApps.add(packageName)
        prefs.edit(commit = true) { putStringSet(KEY_HIDDEN, hiddenApps) }
    }

    fun unhideApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableSet()
        hiddenApps.remove(packageName)
        prefs.edit(commit = true) { putStringSet(KEY_HIDDEN, hiddenApps) }
    }

    fun getHiddenApps(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()
    }

    fun setCustomName(packageName: String, customName: String) {
        prefs.edit(commit = true) { putString("$KEY_CUSTOM_NAME$packageName", customName) }
    }

    fun getCustomName(packageName: String): String? {
        return prefs.getString("$KEY_CUSTOM_NAME$packageName", null)
    }

    fun setFontSize(size: FontSize) {
        prefs.edit { putString(KEY_FONT_SIZE, size.name) }
    }

    fun getFontSize(): FontSize {
        val name = prefs.getString(KEY_FONT_SIZE, FontSize.MEDIUM.name)
        return FontSize.valueOf(name ?: FontSize.MEDIUM.name)
    }

    fun toggleTheme() {
        val isDarkMode = isDarkMode()
        prefs.edit { putBoolean(KEY_DARK_MODE, !isDarkMode) }
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, true) // Default to dark mode
    }

    fun setAppNameTextStyle(style: AppNameTextStyle) {
        prefs.edit { putString(KEY_APP_NAME_TEXT_STYLE, style.name) }
    }

    fun getAppNameTextStyle(): AppNameTextStyle {
        val name = prefs.getString(KEY_APP_NAME_TEXT_STYLE, AppNameTextStyle.LEADING_UPPERCASE.name)
        return AppNameTextStyle.valueOf(name ?: AppNameTextStyle.LEADING_UPPERCASE.name)
    }

    companion object {
        const val KEY_FAVORITES = "favorites"
        private const val KEY_CUSTOM_NAME = "custom_name_"
        const val KEY_HIDDEN = "hidden_apps"
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_STATUS_BAR_VISIBLE = "status_bar_visible"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_APP_NAME_TEXT_STYLE = "app_name_text_style"
    }
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