package com.example.cleanlauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var favoriteAppsView: RecyclerView
    private lateinit var launcherPreferences: LauncherPreferences
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launcherPreferences = LauncherPreferences(requireContext())
        favoriteAppsView = view.findViewById(R.id.favorite_apps)
        favoriteAppsView.layoutManager = LinearLayoutManager(context)

        // Add divider
        val divider = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        context?.let {
            ContextCompat.getDrawable(it, R.drawable.divider)?.let { drawable ->
                divider.setDrawable(drawable)
            }
        }
        favoriteAppsView.addItemDecoration(divider)

        updateFavorites()
    }


    private fun updateFavorites() {
        val fontSize = launcherPreferences.getFontSize()
        val apps = AppUtils.getInstalledApps(requireContext(), launcherPreferences)
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
                if (item is LauncherItem.App) {
                    AppUtils.launchApp(requireContext(), item.appInfo.packageName)
                }
            },
            onItemLongClick = { item, view ->
                if (item is LauncherItem.App) {
                    AppUtils.showAppOptions(requireContext(), item, view, launcherPreferences) {
                        updateFavorites()
                    }
                    true
                } else false
            },
            isFavoritesList = true,
            fontSize = fontSize
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

    private fun startTimeUpdates() {
        scope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val nextMinute = currentTime - (currentTime % 60000) + 60000
                val delayToNextMinute = nextMinute - currentTime

                delay(delayToNextMinute)

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
}