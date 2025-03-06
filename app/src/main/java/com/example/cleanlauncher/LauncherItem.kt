package com.example.cleanlauncher

sealed class LauncherItem {
    data class App(val appInfo: AppInfo) : LauncherItem()
    object AllApps : LauncherItem()
    object Spacer : LauncherItem()
}