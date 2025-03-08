package com.example.cleanlauncher

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class LauncherPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "launcher_prefs", Context.MODE_PRIVATE
    )

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

    // Get the set of favorite apps
    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    // Set a custom name for an app
    fun setCustomName(packageName: String, customName: String) {
        prefs.edit(commit = true) { putString("$KEY_CUSTOM_NAME$packageName", customName) }
    }

    // Get the custom name for an app
    fun getCustomName(packageName: String): String? {
        return prefs.getString("$KEY_CUSTOM_NAME$packageName", null)
    }

    // Hide an app
    fun hideApp(packageName: String) {
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

    // Get the set of hidden apps
    fun getHiddenApps(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()
    }

    // Set the font size preference
    fun setFontSize(size: FontSize) {
        prefs.edit().putString(KEY_FONT_SIZE, size.name).apply()
    }

    // Get the font size preference
    fun getFontSize(): FontSize {
        val name = prefs.getString(KEY_FONT_SIZE, FontSize.MEDIUM.name)
        return FontSize.valueOf(name ?: FontSize.MEDIUM.name)
    }

    companion object {
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_CUSTOM_NAME = "custom_name_"
        private const val KEY_HIDDEN = "hidden_apps"
        private const val KEY_FONT_SIZE = "font_size"
    }
}

enum class FontSize(val textSize: Float) {
    SMALL(20f),
    MEDIUM(30f),
    LARGE(40f),
    XLARGE(50f)
}