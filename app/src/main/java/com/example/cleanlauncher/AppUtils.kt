package com.example.cleanlauncher

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast

object AppUtils {

    fun getInstalledApps(context: Context, launcherPreferences: LauncherPreferences): List<AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfos = context.packageManager.queryIntentActivities(mainIntent, 0)

        return resolveInfos.map { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val originalName = resolveInfo.loadLabel(context.packageManager).toString()
            val customName = launcherPreferences.getCustomName(packageName)
            AppInfo(
                name = originalName,
                packageName = packageName,
                state = determineAppState(packageName, launcherPreferences),
                customName = customName
            )
        }.sortedBy { appInfo ->
            (appInfo.customName ?: appInfo.name).lowercase()
        }
    }

    private fun determineAppState(packageName: String, launcherPreferences: LauncherPreferences): AppState {
        return when {
            launcherPreferences.getFavorites().contains(packageName) -> AppState.FAVORITE
            launcherPreferences.getHiddenApps().contains(packageName) -> AppState.HIDDEN
            else -> AppState.NEITHER
        }
    }

    fun launchApp(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.let { context.startActivity(it) }
    }

    fun showAppOptions(context: Context, app: LauncherItem.App, view: View, launcherPreferences: LauncherPreferences, updateList: () -> Unit) {
        PopupMenu(context, view).apply {
            if (launcherPreferences.getFavorites().contains(app.appInfo.packageName)) {
                menu.add("Remove from Favorites")
            } else {
                menu.add("Add to Favorites")
            }
            menu.add("Rename")
            menu.add("Hide App")

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.title.toString()) {
                    "Add to Favorites" -> {
                        launcherPreferences.addFavorite(app.appInfo.packageName)
                        Toast.makeText(context, "${app.appInfo.displayName()} added to favorites", Toast.LENGTH_SHORT).show()
                        updateList()
                        true
                    }
                    "Remove from Favorites" -> {
                        launcherPreferences.removeFavorite(app.appInfo.packageName)
                        Toast.makeText(context, "${app.appInfo.displayName()} removed from favorites", Toast.LENGTH_SHORT).show()
                        updateList()
                        true
                    }
                    "Rename" -> {
                        showRenameDialog(context, app, launcherPreferences, updateList)
                        true
                    }
                    "Hide App" -> {
                        launcherPreferences.hideApp(app.appInfo.packageName)
                        Toast.makeText(context, "${app.appInfo.displayName()} hidden", Toast.LENGTH_SHORT).show()
                        updateList()
                        true
                    }

                    else -> false
                }
            }
            show()
        }
    }

    private fun showRenameDialog(context: Context, app: LauncherItem.App, launcherPreferences: LauncherPreferences, updateList: () -> Unit) {
        val editText = EditText(context).apply {
            setText(launcherPreferences.getCustomName(app.appInfo.packageName) ?: app.appInfo.name)
            setSingleLine()
        }

        AlertDialog.Builder(context)
            .setTitle("Rename App")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    launcherPreferences.setCustomName(app.appInfo.packageName, newName)
                    updateList()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}