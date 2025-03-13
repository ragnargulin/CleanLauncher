package com.example.cleanlauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cleanlauncher.databinding.FragmentHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var launcherPreferences: LauncherPreferences
    private var cachedFavoriteApps: List<LauncherItem>? = null
    private var lastKnownFontSize: FontSize? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using View Binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launcherPreferences = LauncherPreferences(requireContext())

        binding.favoriteApps.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(createDivider())
        }

        lastKnownFontSize = launcherPreferences.getFontSize()
        updateFavorites(lastKnownFontSize ?: FontSize.MEDIUM)
    }

    override fun onResume() {
        super.onResume()

        val currentFontSize = launcherPreferences.getFontSize()
        if (currentFontSize != lastKnownFontSize) {
            lastKnownFontSize = currentFontSize
            cachedFavoriteApps = null
        }
        updateFavorites(currentFontSize)
        startTimeUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createDivider(): DividerItemDecoration {
        return DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
            context?.let {
                ContextCompat.getDrawable(it, R.drawable.divider)?.let { drawable ->
                    setDrawable(drawable)
                }
            }
        }
    }

    private fun updateFavorites(fontSize: FontSize) {
        val apps = AppUtils.getInstalledApps(requireContext(), launcherPreferences)
        val favoriteApps = apps.filter { it.state == AppState.FAVORITE }

        val launcherItems = favoriteApps.map { LauncherItem.App(it) } +
                if (favoriteApps.isEmpty()) listOf(LauncherItem.AllApps) else emptyList()

        if (cachedFavoriteApps == launcherItems && binding.favoriteApps.adapter != null) {
            (binding.favoriteApps.adapter as AppAdapter).notifyDataSetChanged()
            return
        }

        cachedFavoriteApps = launcherItems

        binding.favoriteApps.adapter = AppAdapter(
            items = launcherItems,
            onItemClick = { item ->
                if (item is LauncherItem.App) {
                    AppUtils.launchApp(requireContext(), item.appInfo.packageName)
                }
            },
            onItemLongClick = { item, _ -> item is LauncherItem.App },
            isFavoritesList = true,
            fontSize = lastKnownFontSize ?: FontSize.MEDIUM,
            launcherPreferences = launcherPreferences,
            showAppState = false
        )
    }

    private fun startTimeUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val nextMinute = currentTime - (currentTime % 60000) + 60000
                val delayToNextMinute = nextMinute - currentTime

                delay(delayToNextMinute)

                val adapter = binding.favoriteApps.adapter as? AppAdapter ?: return@launch
                val clockPosition = adapter.items.indexOfFirst { item ->
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