package com.example.cleanlauncher

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppDrawerActivity : AppCompatActivity() {
    private lateinit var launcherPreferences: LauncherPreferences
    private var startY = 0f
    private lateinit var allAppsView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_drawer)

        launcherPreferences = LauncherPreferences(this)

        allAppsView = findViewById(R.id.all_apps)
        allAppsView.layoutManager = LinearLayoutManager(this)

        updateAppList()

        allAppsView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = e.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!rv.canScrollVertically(-1)) {  // Check if at top
                            val diff = e.y - startY
                            if (diff > 200) { // Swiped down while at top
                                finish()
                                return true
                            }
                        }
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    override fun onResume() {
        super.onResume()
        updateAppList()
    }

    private fun updateAppList() {
        val apps = AppUtils.getInstalledApps(this, launcherPreferences)
            .filter { it.state == AppState.NEITHER }
            .map { LauncherItem.App(it) }

        allAppsView.adapter = AppAdapter(
            items = apps,
            onItemClick = { item ->
                if (item is LauncherItem.App) {
                    AppUtils.launchApp(this, item.appInfo.packageName)
                }
            },
            onItemLongClick = { item, view ->
                if (item is LauncherItem.App) {
                    showAppOptions(item, view)
                    true
                } else false
            },
            isFavoritesList = false,
            fontSize = launcherPreferences.getFontSize()
        )
    }

    private fun showAppOptions(app: LauncherItem.App, view: View) {
        PopupMenu(this, view).apply {
            if (launcherPreferences.getFavorites().contains(app.appInfo.packageName)) {
                menu.add("Remove from Favorites")
            } else {
                menu.add("Add to Favorites")
            }
            menu.add("Rename")
            menu.add("Hide App")
            menu.add("Settings")

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.title.toString()) {
                    "Add to Favorites" -> {
                        launcherPreferences.addFavorite(app.appInfo.packageName)
                        Toast.makeText(this@AppDrawerActivity,
                            "${app.appInfo.displayName()} added to favorites",
                            Toast.LENGTH_SHORT).show()
                        true
                    }
                    "Remove from Favorites" -> {
                        launcherPreferences.removeFavorite(app.appInfo.packageName)
                        Toast.makeText(this@AppDrawerActivity,
                            "${app.appInfo.displayName()} removed from favorites",
                            Toast.LENGTH_SHORT).show()
                        true
                    }
                    "Rename" -> {
                        showRenameDialog(app)
                        true
                    }
                    "Hide App" -> {
                        launcherPreferences.hideApp(app.appInfo.packageName)
                        Toast.makeText(this@AppDrawerActivity,
                            "${app.appInfo.displayName()} hidden",
                            Toast.LENGTH_SHORT).show()
                        updateAppList()
                        true
                    }
                    "Settings" -> {
                        val intent = Intent(this@AppDrawerActivity, SettingsActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showRenameDialog(app: LauncherItem.App) {
        val editText = EditText(this).apply {
            setText(launcherPreferences.getCustomName(app.appInfo.packageName) ?: app.appInfo.name)
            setSingleLine()
        }

        AlertDialog.Builder(this)
            .setTitle("Rename App")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    launcherPreferences.setCustomName(app.appInfo.packageName, newName)
                    updateAppList()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, R.anim.slide_down)
    }
}