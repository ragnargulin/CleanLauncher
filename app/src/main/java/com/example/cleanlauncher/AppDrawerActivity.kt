package com.example.cleanlauncher

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppDrawerActivity : AppCompatActivity() {
    private var startY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_drawer)

        val allAppsView = findViewById<RecyclerView>(R.id.all_apps)
        allAppsView.layoutManager = LinearLayoutManager(this)

        val apps = getInstalledApps().map { LauncherItem.App(it) }
        allAppsView.adapter = AppAdapter(
            items = apps,
            onItemClick = { item ->
                if (item is LauncherItem.App) {
                    launchApp(item.appInfo.packageName)
                }
            },
            onItemLongClick = { item, view ->
                if (item is LauncherItem.App) {
                    showAppOptions(item, view)
                    true
                } else false
            }
        )

        allAppsView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = e.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!rv.canScrollVertically(-1)) {  // Check if at top
                            val diff = e.y - startY
                            if (diff > 100) { // Swiped down while at top
                                finish()
                                return true
                            }
                        }
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                // Not needed
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                // Not needed
            }
        })
    }

    private fun showAppOptions(app: LauncherItem.App, view: View) {
        PopupMenu(this, view).apply {
            menu.add("Add to Favorites")
            menu.add("Hide App")

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.title.toString()) {
                    "Add to Favorites" -> {
                        Toast.makeText(this@AppDrawerActivity,
                            "${app.appInfo.name} added to favorites",
                            Toast.LENGTH_SHORT).show()
                        true
                    }
                    "Hide App" -> {
                        Toast.makeText(this@AppDrawerActivity,
                            "${app.appInfo.name} hidden",
                            Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_down)
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

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.let { startActivity(it) }
    }
}