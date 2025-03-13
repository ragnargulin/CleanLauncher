package com.example.cleanlauncher

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.util.TypedValue
import com.example.cleanlauncher.databinding.FragmentAppDrawerBinding


class AppDrawerFragment : Fragment() {

    private var _binding: FragmentAppDrawerBinding? = null
    private val binding get() = _binding!!

    private lateinit var launcherPreferences: LauncherPreferences
    private var cachedAppList: List<LauncherItem.App>? = null
    private var lastKnownFontSize: FontSize? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launcherPreferences = LauncherPreferences(requireContext())

        binding.allApps.apply {
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = true
            addOnItemTouchListener(createTouchListener())
        }

        binding.searchBar.addTextChangedListener(createTextWatcher())

        binding.settingsIcon.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        lastKnownFontSize = launcherPreferences.getFontSize()
        updateAppList(lastKnownFontSize ?: FontSize.MEDIUM)
    }

    override fun onResume() {
        super.onResume()

        val currentFontSize = launcherPreferences.getFontSize()
        if (currentFontSize != lastKnownFontSize) {
            lastKnownFontSize = currentFontSize
            cachedAppList = null
        }
        updateAppList(currentFontSize)
        binding.searchBar.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun scrollToTop() {
        binding.allApps.scrollToPosition(0)
    }

    private fun filterApps(query: String) {
        val filteredList = cachedAppList?.filter {
            it.appInfo.name.contains(query, ignoreCase = true)
        } ?: emptyList()

        (binding.allApps.adapter as? AppAdapter)?.updateList(filteredList)
    }

    private fun updateAppList(fontSize: FontSize) {
        binding.searchBar.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.textSize)

        val apps = AppUtils.getInstalledApps(requireContext(), launcherPreferences)
            .filter { it.state == AppState.NEITHER }
            .map { LauncherItem.App(it) }

        cachedAppList = apps

        binding.allApps.adapter = AppAdapter(
            items = apps,
            onItemClick = { item ->
                if (item is LauncherItem.App) {
                    AppUtils.launchApp(requireContext(), item.appInfo.packageName)
                }
            },
            onItemLongClick = { item, _ -> item is LauncherItem.App },
            isFavoritesList = false,
            fontSize = fontSize,
            launcherPreferences = launcherPreferences,
            showAppState = false
        )
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterApps(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    private fun createTouchListener(): RecyclerView.OnItemTouchListener {
        return object : RecyclerView.OnItemTouchListener {
            private var startY = 0f

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = e.y
                        (activity as? MainActivity)?.binding?.viewPager?.isUserInputEnabled = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val diffY = e.y - startY
                        if (diffY > 0 && !rv.canScrollVertically(-1)) {
                            (activity as? MainActivity)?.binding?.viewPager?.isUserInputEnabled = true
                            return false
                        }
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        }
    }
}