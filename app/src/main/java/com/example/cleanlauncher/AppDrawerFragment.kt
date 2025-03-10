package com.example.cleanlauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.widget.TextView

class AppDrawerFragment : Fragment() {

    private lateinit var allAppsView: RecyclerView
    private lateinit var launcherPreferences: LauncherPreferences
    private var cachedAppList: List<LauncherItem.App>? = null
    private var lastKnownFontSize: FontSize? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_app_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launcherPreferences = LauncherPreferences(requireContext())
        allAppsView = view.findViewById(R.id.all_apps)
        allAppsView.layoutManager = LinearLayoutManager(context)
        allAppsView.isNestedScrollingEnabled = true

        val settingsIcon: TextView = view.findViewById(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        lastKnownFontSize = launcherPreferences.getFontSize()
        updateAppList(lastKnownFontSize!!)
        // Add touch listener to handle swipe gestures
        allAppsView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            private var startY = 0f

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = e.y
                        // Disable ViewPager2 input initially
                        (activity as? MainActivity)?.viewPager?.isUserInputEnabled = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val diffY = e.y - startY
                        // Enable ViewPager2 only if swiping down and at the top of the RecyclerView
                        if (diffY > 0 && !rv.canScrollVertically(-1)) {
                            (activity as? MainActivity)?.viewPager?.isUserInputEnabled = true
                            return false
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

        // Check if the font size has changed
        val currentFontSize = launcherPreferences.getFontSize()
        if (currentFontSize != lastKnownFontSize) {
            lastKnownFontSize = currentFontSize
            cachedAppList = null // Invalidate cache
            updateAppList(currentFontSize)
        } else {
            updateAppList(currentFontSize)
        }
        allAppsView.scrollToPosition(0)
    }

    private fun updateAppList(fontSize: FontSize) {
        val apps = AppUtils.getInstalledApps(requireContext(), launcherPreferences)
            .filter { it.state == AppState.NEITHER }
            .map { LauncherItem.App(it) }

        // Check if the app list has changed
        if (cachedAppList == apps && allAppsView.adapter != null) {
            // If the list is unchanged, just update the font size
            (allAppsView.adapter as AppAdapter).notifyDataSetChanged()
            return
        }

        // Update the cache
        cachedAppList = apps

        allAppsView.adapter = AppAdapter(
            items = apps,
            onItemClick = { item ->
                if (item is LauncherItem.App) {
                    AppUtils.launchApp(requireContext(), item.appInfo.packageName)
                }
            },
            onItemLongClick = { item, view ->
                if (item is LauncherItem.App) {
                    AppUtils.showAppOptions(requireContext(), item, view, launcherPreferences) {
                        cachedAppList = null // Invalidate cache on app options change
                        updateAppList(fontSize)
                    }
                    true
                } else false
            },
            isFavoritesList = false,
            fontSize = fontSize
        )
    }

}