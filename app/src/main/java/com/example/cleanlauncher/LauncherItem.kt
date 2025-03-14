package com.example.cleanlauncher

sealed class LauncherItem {
    data class App(val appInfo: AppInfo) : LauncherItem()
    data object AllApps : LauncherItem()
}