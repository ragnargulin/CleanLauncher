package com.example.cleanlauncher

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cleanlauncher.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var launcherPreferences: LauncherPreferences
    private var cachedFavoriteApps: List<LauncherItem.App>? = null
    private var cachedAllApps: List<LauncherItem.App>? = null
    private var lastKnownFontSize: FontSize? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launcherPreferences = LauncherPreferences.getInstance(this)

        // Adjust padding for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        applyTheme()
        setupRecyclerView()
        setupSearchBar()
        setupSettingsButton()

        lastKnownFontSize = launcherPreferences.getFontSize()
        updateAppLists(lastKnownFontSize ?: FontSize.MEDIUM)
        displayFavoriteApps()

    }


    override fun onPause() {
        super.onPause()
        binding.searchBar.clearFocus()

    }
    override fun onResume() {
        super.onResume()
        val currentFontSize = launcherPreferences.getFontSize()
        if (currentFontSize != lastKnownFontSize) {
            lastKnownFontSize = currentFontSize
            cachedFavoriteApps = null
            cachedAllApps = null
        }
        updateAppLists(currentFontSize)
        applyTheme()
        binding.searchBar.setText("")

    }

    private fun applyTheme() {
        val desiredMode = if (launcherPreferences.isDarkMode()) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (AppCompatDelegate.getDefaultNightMode() != desiredMode) {
            AppCompatDelegate.setDefaultNightMode(desiredMode)
        }
    }

    private fun setupRecyclerView() {
        binding.favoriteApps.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            addItemDecoration(createDivider())
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        binding.searchResults.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            addItemDecoration(createDivider())
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }
    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(createTextWatcher())
    }

    private fun setupSettingsButton() {
        binding.settingsIcon.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun createDivider(): DividerItemDecoration {
        return DividerItemDecoration(this, LinearLayoutManager.VERTICAL).apply {
            ContextCompat.getDrawable(this@HomeActivity, R.drawable.divider)?.let { drawable ->
                setDrawable(drawable)
            }
        }
    }

    private fun updateAppLists(fontSize: FontSize) {
        binding.searchBar.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.textSize)
        val apps = AppUtils.getInstalledApps(this, launcherPreferences)
        cachedFavoriteApps = apps.filter { it.state == AppState.FAVORITE }.map { LauncherItem.App(it) }
        cachedAllApps = apps.map { LauncherItem.App(it) }
    }

    private fun displayFavoriteApps() {
        binding.favoriteApps.adapter = AppAdapter(
            items = cachedFavoriteApps ?: emptyList(),
            onItemClick = { item ->
                if (item is LauncherItem.App) {
                    AppUtils.launchApp(this, item.appInfo.packageName)
                }
            },
            onItemLongClick = { item, _ -> item is LauncherItem.App },
            isFavoritesList = true,
            fontSize = lastKnownFontSize ?: FontSize.MEDIUM,
            launcherPreferences = launcherPreferences,
            showAppState = false
        )
    }

    private fun filterApps(query: String) {
        val filteredList = cachedAllApps?.filter {
            it.appInfo.name.startsWith(query, ignoreCase = true)
        } ?: emptyList()

        binding.searchResults.adapter = AppAdapter(
            items = filteredList,
            onItemClick = { item ->
                if (item is LauncherItem.App) {
                    AppUtils.launchApp(this, item.appInfo.packageName)
                }
            },
            onItemLongClick = { item, _ -> item is LauncherItem.App },
            isFavoritesList = false,
            fontSize = lastKnownFontSize ?: FontSize.MEDIUM,
            launcherPreferences = launcherPreferences,
            showAppState = false
        )
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    displayFavoriteApps()
                    binding.favoriteApps.visibility = View.VISIBLE
                    binding.searchResults.visibility = View.GONE
                } else {
                    filterApps(s.toString())
                    binding.favoriteApps.visibility = View.GONE
                    binding.searchResults.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }
}