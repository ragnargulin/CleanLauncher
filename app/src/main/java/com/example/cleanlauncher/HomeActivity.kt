package com.example.cleanlauncher

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        val statusBarHeight = getStatusBarHeight()
        binding.root.setPadding(0, statusBarHeight, 0, 0)


        setupRecyclerView()
        setupSearchBar()
        setupSettingsButton()

        lastKnownFontSize = launcherPreferences.getFontSize()
        updateAppLists(lastKnownFontSize ?: FontSize.MEDIUM)
        displayFavoriteApps()
        setStatusBarVisibility(launcherPreferences.isStatusBarVisible())
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    private fun setStatusBarVisibility(isVisible: Boolean) {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            if (isVisible) {
                show(WindowInsetsCompat.Type.statusBars())
            } else {
                hide(WindowInsetsCompat.Type.statusBars())
            }
        }
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
        binding.searchBar.setText("")
        setStatusBarVisibility(launcherPreferences.isStatusBarVisible())

    }

    private fun setupRecyclerView() {
        setupRecyclerView(binding.favoriteApps)
        setupRecyclerView(binding.searchResults)
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
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

    private fun updateAppStates(apps: List<AppInfo>, launcherPreferences: LauncherPreferences) {
        val favorites = launcherPreferences.getFavorites()
        val hiddenApps = launcherPreferences.getHiddenApps()
        val badApps = launcherPreferences.getBadApps()

        apps.forEach { appInfo ->
            appInfo.state = when {
                badApps.contains(appInfo.packageName) -> AppState.BAD
                favorites.contains(appInfo.packageName) -> AppState.FAVORITE
                hiddenApps.contains(appInfo.packageName) -> AppState.HIDDEN
                else -> AppState.NEITHER
            }
        }
    }

    private fun updateAppLists(fontSize: FontSize) {
        binding.searchBar.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.textSize)
        val apps = AppUtils.getInstalledApps(this, launcherPreferences)
        updateAppStates(apps, launcherPreferences) // Ensure states are updated
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
        val filteredList = cachedAllApps?.filterNot {
            it.appInfo.state == AppState.FAVORITE || it.appInfo.state == AppState.HIDDEN
        }?.filter {
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