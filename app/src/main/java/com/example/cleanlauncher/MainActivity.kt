package com.example.cleanlauncher

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var favoriteAppsView: RecyclerView
    private var startY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        favoriteAppsView = findViewById(R.id.favorite_apps)

        // Set up RecyclerView
        favoriteAppsView.layoutManager = LinearLayoutManager(this)

        val apps = getInstalledApps()
        val favoriteApps = apps.take(5).map { LauncherItem.App(it) }
        val launcherItems = listOf(LauncherItem.Spacer) + favoriteApps + LauncherItem.AllApps

        favoriteAppsView.adapter = AppAdapter(
            items = launcherItems,
            onItemClick = { item ->
                when (item) {
                    is LauncherItem.App -> launchApp(item.appInfo.packageName)
                    LauncherItem.AllApps -> {
                        val intent = Intent(this, AppDrawerActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_up, 0)
                    }
                    LauncherItem.Spacer -> { /* Do nothing */ }
                }
            },
            onItemLongClick = { _, _ -> false } // No long-press behavior in main screen yet
        )

        favoriteAppsView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = e.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val diff = e.y - startY
                        if (diff < -100) { // Swiped up
                            val intent = Intent(this@MainActivity, AppDrawerActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_up, 0)
                            return true
                        }
                    }
                }
                return false // Let the RecyclerView handle other touches
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                // Not needed
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                // Not needed
            }
        })
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