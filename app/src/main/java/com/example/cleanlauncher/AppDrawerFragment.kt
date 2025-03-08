package com.example.cleanlauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppDrawerFragment : Fragment() {

    private lateinit var allAppsView: RecyclerView
    private lateinit var launcherPreferences: LauncherPreferences

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

        updateAppList()
    }

    fun updateFontSize() {
        updateAppList()
    }

    private fun updateAppList() {
        val fontSize = launcherPreferences.getFontSize()
        val apps = AppUtils.getInstalledApps(requireContext(), launcherPreferences)
            .filter { it.state == AppState.NEITHER }
            .map { LauncherItem.App(it) }

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
                        updateAppList()
                    }
                    true
                } else false
            },
            isFavoritesList = false,
            fontSize = fontSize
        )
    }
}