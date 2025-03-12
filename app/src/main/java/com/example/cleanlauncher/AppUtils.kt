package com.example.cleanlauncher

import android.content.Context
import android.content.Intent

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
}