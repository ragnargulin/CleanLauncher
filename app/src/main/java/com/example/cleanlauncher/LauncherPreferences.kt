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

    // Add an app to favorites
    fun addFavorite(packageName: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(packageName)
        prefs.edit(commit = true) { putStringSet(KEY_FAVORITES, favorites) }
    }

    // Remove an app from favorites
    fun removeFavorite(packageName: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(packageName)
        prefs.edit(commit = true) { putStringSet(KEY_FAVORITES, favorites) }
    }

    // Hide an app
    fun hideApp(packageName: String) {
        removeFavorite(packageName)  // Ensure it's not a favorite
        val hiddenApps = getHiddenApps().toMutableSet()
        hiddenApps.add(packageName)
        prefs.edit(commit = true) { putStringSet(KEY_HIDDEN, hiddenApps) }
    }

    // Unhide an app
    fun unhideApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableSet()
        hiddenApps.remove(packageName)
        prefs.edit(commit = true) { putStringSet(KEY_HIDDEN, hiddenApps) }
    }

    // Get the set of favorite apps
    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    // Get the set of hidden apps
    fun getHiddenApps(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()
    }

    // Set a custom name for an app
    fun setCustomName(packageName: String, customName: String) {
        prefs.edit(commit = true) { putString("$KEY_CUSTOM_NAME$packageName", customName) }
    }

    // Get the custom name for an app
    fun getCustomName(packageName: String): String? {
        return prefs.getString("$KEY_CUSTOM_NAME$packageName", null)
    }

    // Set the font size preference
    fun setFontSize(size: FontSize) {
        prefs.edit { putString(KEY_FONT_SIZE, size.name) }
    }

    // Get the font size preference
    fun getFontSize(): FontSize {
        val name = prefs.getString(KEY_FONT_SIZE, FontSize.MEDIUM.name)
        return FontSize.valueOf(name ?: FontSize.MEDIUM.name)
    }

    companion object {
        const val KEY_FAVORITES = "favorites"
        private const val KEY_CUSTOM_NAME = "custom_name_"
        const val KEY_HIDDEN = "hidden_apps"
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_STATUS_BAR_VISIBLE = "status_bar_visible"
    }
}

