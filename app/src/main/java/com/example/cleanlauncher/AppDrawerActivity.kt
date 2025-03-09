package com.example.cleanlauncher
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class AppDrawerActivity : AppCompatActivity() {
    private lateinit var launcherPreferences: LauncherPreferences
    private var startY = 0f
    private lateinit var allAppsView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_drawer)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
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
        allAppsView.scrollToPosition(0)

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
                    AppUtils.showAppOptions(this, item, view, launcherPreferences) {
                        updateAppList()
                    }
                    true
                } else false
            },
            isFavoritesList = false,
            fontSize = launcherPreferences.getFontSize()
        )
    }
}