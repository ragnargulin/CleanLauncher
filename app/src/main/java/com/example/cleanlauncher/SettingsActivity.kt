package com.example.cleanlauncher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsActivity : AppCompatActivity() {
    private lateinit var launcherPreferences: LauncherPreferences
    private lateinit var hiddenAppsView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        launcherPreferences = LauncherPreferences(this)
        hiddenAppsView = findViewById(R.id.hidden_apps)
        hiddenAppsView.layoutManager = LinearLayoutManager(this)

        updateHiddenAppsList()
    }

    private fun updateHiddenAppsList() {
        val hiddenApps = getInstalledApps()
            .filter { launcherPreferences.isHidden(it.packageName) }
            .map { appInfo ->
                val customName = launcherPreferences.getCustomName(appInfo.packageName)
                if (customName != null) {
                    appInfo.copy(name = customName)
                } else {
                    appInfo
                }
            }
            .map { LauncherItem.App(it) }

        hiddenAppsView.adapter = AppAdapter(
            items = hiddenApps,
            onItemClick = { /* Do nothing */ },
            onItemLongClick = { item, _ ->
                if (item is LauncherItem.App) {
                    showUnhideOption(item)
                    true
                } else false
            }
        )
    }

    private fun showUnhideOption(app: LauncherItem.App) {
        AlertDialog.Builder(this)
            .setTitle("Unhide App")
            .setMessage("Do you want to unhide ${app.appInfo.name}?")
            .setPositiveButton("Unhide") { _, _ ->
                launcherPreferences.unhideApp(app.appInfo.packageName)
                updateHiddenAppsList()
                Toast.makeText(this, "${app.appInfo.name} is now visible", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getInstalledApps(): List<AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        return resolveInfos.map { resolveInfo ->
            AppInfo(
                name = resolveInfo.loadLabel(packageManager).toString(),
                packageName = resolveInfo.activityInfo.packageName
            )
        }.sortedBy { it.name }
    }

    override fun onBackPressed() {
        finish()
    }
}