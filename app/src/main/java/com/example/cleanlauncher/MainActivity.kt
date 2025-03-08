package com.example.cleanlauncher

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelChildren
import android.app.ActivityOptions

class MainActivity : AppCompatActivity() {
    private lateinit var favoriteAppsView: RecyclerView
    private lateinit var launcherPreferences: LauncherPreferences
    private var startY = 0f
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launcherPreferences = LauncherPreferences(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        favoriteAppsView = findViewById(R.id.favorite_apps)
        favoriteAppsView.layoutManager = LinearLayoutManager(this)

        // Add divider
        val divider = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.divider)?.let {
            divider.setDrawable(it)
        }
        favoriteAppsView.addItemDecoration(divider)

        updateFavorites()

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
                            startActivity(intent, ActivityOptions.makeCustomAnimation(
                                this@MainActivity,
                                R.anim.slide_up,
                                0
                            ).toBundle())
                            return true
                        }
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    private fun startTimeUpdates() {
        scope.launch {
            while (isActive) {
                // Calculate delay until next minute
                val currentTime = System.currentTimeMillis()
                val nextMinute = currentTime - (currentTime % 60000) + 60000
                val delayToNextMinute = nextMinute - currentTime

                // Wait until next minute
                delay(delayToNextMinute)

                // Update clock
                val adapter = favoriteAppsView.adapter
                val items = (adapter as? AppAdapter)?.items ?: return@launch
                val clockPosition = items.indexOfFirst { item ->
                    item is LauncherItem.App &&
                            (item.appInfo.packageName == "com.android.deskclock" ||
                                    item.appInfo.packageName == "com.google.android.deskclock")
                }

                if (clockPosition != -1) {
                    adapter.notifyItemChanged(clockPosition)
                }
            }
        }
    }

    private fun updateFavorites() {
        val apps = AppUtils.getInstalledApps(this, launcherPreferences)
        val favoriteApps = apps.filter { it.state == AppState.FAVORITE }

        val launcherItems = favoriteApps.map { LauncherItem.App(it) } +
                if (favoriteApps.isEmpty()) {
                    listOf(LauncherItem.AllApps)
                } else {
                    emptyList()
                }

        favoriteAppsView.adapter = AppAdapter(
            items = launcherItems,
            onItemClick = { item ->
                when (item) {
                    is LauncherItem.App -> {
                        AppUtils.launchApp(this, item.appInfo.packageName)
                    }
                    LauncherItem.AllApps -> {
                        val intent = Intent(this, AppDrawerActivity::class.java)
                        startActivity(intent, ActivityOptions.makeCustomAnimation(
                            this,
                            R.anim.slide_up,
                            0
                        ).toBundle())
                    }
                }
            },
            onItemLongClick = { item, view ->
                if (item is LauncherItem.App) {
                    AppUtils.showAppOptions(this, item, view, launcherPreferences) {
                        updateFavorites()
                    }
                    true
                } else false
            },
            isFavoritesList = true,
            fontSize = launcherPreferences.getFontSize()
        )
    }

    override fun onResume() {
        super.onResume()
        updateFavorites()
        startTimeUpdates()
    }

    override fun onPause() {
        super.onPause()
        scope.coroutineContext.cancelChildren()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.coroutineContext.cancelChildren()
    }
}