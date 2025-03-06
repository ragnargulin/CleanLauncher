package com.example.cleanlauncher

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class LauncherPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "launcher_prefs", Context.MODE_PRIVATE
    )

    fun addFavorite(packageName: String, customName: String? = null) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(packageName)
        prefs.edit { putStringSet(KEY_FAVORITES, favorites) }

        if (customName != null) {
            setCustomName(packageName, customName)
        }
    }

    fun removeFavorite(packageName: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(packageName)
        prefs.edit {
            putStringSet(KEY_FAVORITES, favorites)
                .remove("$KEY_CUSTOM_NAME$packageName")
        }
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(packageName: String): Boolean {
        return getFavorites().contains(packageName)
    }

    fun setCustomName(packageName: String, customName: String) {
        prefs.edit { putString("$KEY_CUSTOM_NAME$packageName", customName) }
    }

    fun getCustomName(packageName: String): String? {
        return prefs.getString("$KEY_CUSTOM_NAME$packageName", null)
    }

    fun hideApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableSet()
        hiddenApps.add(packageName)
        prefs.edit { putStringSet(KEY_HIDDEN, hiddenApps) }
    }

    fun unhideApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableSet()
        hiddenApps.remove(packageName)
        prefs.edit { putStringSet(KEY_HIDDEN, hiddenApps) }
    }

    private fun getHiddenApps(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()
    }

    fun isHidden(packageName: String): Boolean {
        return getHiddenApps().contains(packageName)
    }

    companion object {
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_CUSTOM_NAME = "custom_name_"
        private const val KEY_HIDDEN = "hidden_apps"
    }
}