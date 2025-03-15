package com.example.cleanlauncher

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class LauncherPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "launcher_prefs", Context.MODE_PRIVATE
    )

    fun isStatusBarVisible(): Boolean = prefs.getBoolean(KEY_STATUS_BAR_VISIBLE, true)

    fun setStatusBarVisible(isVisible: Boolean) {
        prefs.edit { putBoolean(KEY_STATUS_BAR_VISIBLE, isVisible) }
    }

    fun addFavorite(packageName: String) {
        clearAllStates(packageName)
        val favorites = getFavorites().toMutableSet()
        favorites.add(packageName)
        prefs.edit { putStringSet(KEY_FAVORITES, favorites) }
    }

    fun removeFavorite(packageName: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(packageName)
        prefs.edit { putStringSet(KEY_FAVORITES, favorites) }
    }

    fun getFavorites(): Set<String> = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    fun hideApp(packageName: String) {
        clearAllStates(packageName)
        val hiddenApps = getHiddenApps().toMutableSet()
        hiddenApps.add(packageName)
        prefs.edit { putStringSet(KEY_HIDDEN, hiddenApps) }
    }

    fun unhideApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableSet()
        hiddenApps.remove(packageName)
        prefs.edit { putStringSet(KEY_HIDDEN, hiddenApps) }
    }

    fun getHiddenApps(): Set<String> = prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()

    fun markAsBad(packageName: String) {
        clearAllStates(packageName)
        val badApps = getBadApps().toMutableSet()
        badApps.add(packageName)
        prefs.edit { putStringSet(KEY_BAD, badApps) }

        println("Marked as BAD: $packageName")

    }

    fun removeBad(packageName: String) {
        val badApps = getBadApps().toMutableSet()
        badApps.remove(packageName)
        prefs.edit { putStringSet(KEY_BAD, badApps) }
    }

    fun getBadApps(): Set<String> = prefs.getStringSet(KEY_BAD, emptySet()) ?: emptySet()

    private fun clearAllStates(packageName: String) {
        removeFavorite(packageName)
        unhideApp(packageName)
        removeBad(packageName)
    }

    fun setCustomName(packageName: String, customName: String) {
        prefs.edit { putString("$KEY_CUSTOM_NAME$packageName", customName) }
    }

    fun getCustomName(packageName: String): String? = prefs.getString("$KEY_CUSTOM_NAME$packageName", null)

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

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)

    fun setAppNameTextStyle(style: AppNameTextStyle) {
        prefs.edit { putString(KEY_APP_NAME_TEXT_STYLE, style.name) }
    }

    fun getAppNameTextStyle(): AppNameTextStyle {
        val name = prefs.getString(KEY_APP_NAME_TEXT_STYLE, AppNameTextStyle.LEADING_UPPERCASE.name)
        return AppNameTextStyle.valueOf(name ?: AppNameTextStyle.LEADING_UPPERCASE.name)
    }

    companion object {
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_CUSTOM_NAME = "custom_name_"
        private const val KEY_HIDDEN = "hidden_apps"
        private const val KEY_BAD = "bad_apps"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_STATUS_BAR_VISIBLE = "status_bar_visible"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_APP_NAME_TEXT_STYLE = "app_name_text_style"

        @Volatile
        private var INSTANCE: LauncherPreferences? = null

        fun getInstance(context: Context): LauncherPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LauncherPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
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