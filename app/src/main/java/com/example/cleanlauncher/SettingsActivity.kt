package com.example.cleanlauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cleanlauncher.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var launcherPreferences: LauncherPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use singleton instance
        launcherPreferences = LauncherPreferences.getInstance(this)

        initializeViews()
        setupInitialStates()
        setupListeners()
        updateHiddenAppsList()
    }

    private fun initializeViews() {
        binding.hiddenApps.layoutManager = LinearLayoutManager(this)
    }

    private fun setupInitialStates() {
        binding.statusBarToggle.isChecked = launcherPreferences.isStatusBarVisible()
        setStatusBarVisibility(binding.statusBarToggle.isChecked)

        binding.themeToggle.isChecked = launcherPreferences.isDarkMode()

        binding.lowerCaseToggle.isChecked = launcherPreferences.getAppNameTextStyle() == AppNameTextStyle.LEADING_UPPERCASE

        setupFontSizeSelector()
    }

    private fun setupListeners() {
        binding.statusBarToggle.setOnCheckedChangeListener { _, isChecked ->
            launcherPreferences.setStatusBarVisible(isChecked)
            setStatusBarVisibility(isChecked)
        }

        binding.themeToggle.setOnCheckedChangeListener { _, isChecked ->
            launcherPreferences.toggleTheme()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate()
        }

        binding.lowerCaseToggle.setOnCheckedChangeListener { _, isChecked ->
            val newStyle = if (isChecked) AppNameTextStyle.LEADING_UPPERCASE else AppNameTextStyle.ALL_LOWERCASE
            launcherPreferences.setAppNameTextStyle(newStyle)
        }

        setupCollapsibleSections()
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

    private fun setupFontSizeSelector() {
        val currentSize = launcherPreferences.getFontSize()
        val buttonId = when (currentSize) {
            FontSize.SMALL -> R.id.sizeSmall
            FontSize.MEDIUM -> R.id.sizeMedium
            FontSize.LARGE -> R.id.sizeLarge
            FontSize.XLARGE -> R.id.sizeXLarge
        }
        binding.fontSizeGroup.check(buttonId)

        binding.fontSizeGroup.setOnCheckedChangeListener { _, checkedId ->
            val newSize = when (checkedId) {
                R.id.sizeSmall -> FontSize.SMALL
                R.id.sizeMedium -> FontSize.MEDIUM
                R.id.sizeLarge -> FontSize.LARGE
                R.id.sizeXLarge -> FontSize.XLARGE
                else -> FontSize.MEDIUM
            }
            launcherPreferences.setFontSize(newSize)
            restartHomeActivity()
        }
    }

    private fun setupCollapsibleSections() {
        binding.fontSizeHeader.setOnClickListener {
            toggleVisibility(binding.fontSizeGroup)
        }

        binding.togglesHeader.setOnClickListener {
            toggleVisibility(binding.togglesContainer)
        }

        binding.allAppsHeader.setOnClickListener {
            toggleVisibility(binding.hiddenApps)
        }
    }

    private fun toggleVisibility(view: View) {
        view.visibility = if (view.isVisible) View.GONE else View.VISIBLE
    }

    private fun restartHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun showAppOptions(
        context: Context,
        app: LauncherItem.App,
        view: View,
        launcherPreferences: LauncherPreferences,
        updateList: () -> Unit
    ) {
        PopupMenu(context, view).apply {
            val isFavorite = launcherPreferences.getFavorites().contains(app.appInfo.packageName)
            menu.add(if (isFavorite) "Remove from Favorites" else "Add to Favorites")

            val isHidden = app.appInfo.state == AppState.HIDDEN
            menu.add(if (isHidden) "Unhide App" else "Hide App")

            menu.add("Rename")
            menu.add("App Info")

            setOnMenuItemClickListener { menuItem ->
                handleMenuItemClick(menuItem.title.toString(), context, app, launcherPreferences, updateList)
            }
            show()
        }
    }

    private fun handleMenuItemClick(
        title: String,
        context: Context,
        app: LauncherItem.App,
        launcherPreferences: LauncherPreferences,
        updateList: () -> Unit
    ): Boolean {
        return when (title) {
            "Add to Favorites" -> {
                launcherPreferences.addFavorite(app.appInfo.packageName)
                showToast(context, "${app.appInfo.displayName()} added to favorites")
                updateList()
                true
            }
            "Remove from Favorites" -> {
                launcherPreferences.removeFavorite(app.appInfo.packageName)
                showToast(context, "${app.appInfo.displayName()} removed from favorites")
                updateList()
                true
            }
            "Rename" -> {
                showRenameDialog(context, app, launcherPreferences, updateList)
                true
            }
            "Hide App" -> {
                launcherPreferences.hideApp(app.appInfo.packageName)
                showToast(context, "${app.appInfo.displayName()} hidden")
                updateList()
                true
            }
            "Unhide App" -> {
                launcherPreferences.unhideApp(app.appInfo.packageName)
                showToast(context, "${app.appInfo.displayName()} is now visible")
                updateList()
                true
            }
            "App Info" -> {
                openAppInfo(context, app.appInfo.packageName)
                true
            }
            else -> false
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun openAppInfo(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        context.startActivity(intent)
    }

    private fun showRenameDialog(
        context: Context,
        app: LauncherItem.App,
        launcherPreferences: LauncherPreferences,
        updateList: () -> Unit
    ) {
        val editText = EditText(context).apply {
            setText(launcherPreferences.getCustomName(app.appInfo.packageName) ?: app.appInfo.name)
            setSingleLine()
        }

        android.app.AlertDialog.Builder(context)
            .setTitle("Rename App")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    launcherPreferences.setCustomName(app.appInfo.packageName, newName)
                    updateList()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateHiddenAppsList() {
        val allApps = AppUtils.getInstalledApps(this, launcherPreferences)
            .map { LauncherItem.App(it) }

        binding.hiddenApps.adapter = AppAdapter(
            items = allApps,
            onItemClick = { /* Do nothing */ },
            onItemLongClick = { item, view ->
                if (item is LauncherItem.App) {
                    showAppOptions(
                        context = this,
                        app = item,
                        view = view,
                        launcherPreferences = launcherPreferences,
                        updateList = { updateHiddenAppsList() }
                    )
                    true
                } else false
            },
            isFavoritesList = false,
            fontSize = launcherPreferences.getFontSize(),
            launcherPreferences = launcherPreferences,
            showAppState = true
        )
    }
}