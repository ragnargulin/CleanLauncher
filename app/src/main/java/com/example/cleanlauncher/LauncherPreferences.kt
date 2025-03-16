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
        updateAppState(packageName, KEY_FAVORITES)
    }

    fun removeFavorite(packageName: String) {
        removeAppState(packageName, KEY_FAVORITES)
    }

    fun getFavorites(): Set<String> = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    fun hideApp(packageName: String) {
        updateAppState(packageName, KEY_HIDDEN)
    }

    fun unhideApp(packageName: String) {
        removeAppState(packageName, KEY_HIDDEN)
    }

    fun getHiddenApps(): Set<String> = prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()

    fun markAsBad(packageName: String) {
        updateAppState(packageName, KEY_BAD)
    }

    fun removeBad(packageName: String) {
        removeAppState(packageName, KEY_BAD)
    }

    fun getBadApps(): Set<String> = prefs.getStringSet(KEY_BAD, emptySet()) ?: emptySet()

    private fun updateAppState(packageName: String, key: String) {
        clearAllStates(packageName)
        val apps = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
        apps.add(packageName)
        prefs.edit { putStringSet(key, apps) }
    }

    private fun removeAppState(packageName: String, key: String) {
        val apps = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
        apps.remove(packageName)
        prefs.edit { putStringSet(key, apps) }
    }

    private fun clearAllStates(packageName: String) {
        prefs.edit {
            removeAppState(packageName, KEY_FAVORITES)
            removeAppState(packageName, KEY_HIDDEN)
            removeAppState(packageName, KEY_BAD)
        }
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