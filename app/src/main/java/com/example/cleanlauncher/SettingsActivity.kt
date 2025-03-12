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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial


class SettingsActivity : AppCompatActivity() {
    private lateinit var launcherPreferences: LauncherPreferences
    private lateinit var hiddenAppsView: RecyclerView
    private lateinit var fontSizeGroup: RadioGroup
    private lateinit var statusBarToggle: SwitchMaterial
    private lateinit var themeToggle: SwitchMaterial
    private lateinit var lowerCaseToggle: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        launcherPreferences = LauncherPreferences(this)
        hiddenAppsView = findViewById(R.id.hidden_apps)
        hiddenAppsView.layoutManager = LinearLayoutManager(this)
        fontSizeGroup = findViewById(R.id.fontSizeGroup)
        statusBarToggle = findViewById(R.id.statusBarToggle)
        themeToggle = findViewById(R.id.themeToggle)
        lowerCaseToggle = findViewById(R.id.lowerCaseToggle)

        // Set initial status bar visibility based on preferences
        val isStatusBarVisible = launcherPreferences.isStatusBarVisible()
        statusBarToggle.isChecked = isStatusBarVisible
        setStatusBarVisibility(isStatusBarVisible)

        statusBarToggle.setOnCheckedChangeListener { _, isChecked ->
            launcherPreferences.setStatusBarVisible(isChecked)
            setStatusBarVisibility(isChecked)
        }

        // Set initial theme toggle state
        themeToggle.isChecked = launcherPreferences.isDarkMode()

        // Handle theme toggle changes
        themeToggle.setOnCheckedChangeListener { _, isChecked ->
            launcherPreferences.toggleTheme()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate() // Recreate the activity to apply the new theme
        }

        // Set initial app name text style toggle state
        lowerCaseToggle.isChecked = launcherPreferences.getAppNameTextStyle() == AppNameTextStyle.LEADING_UPPERCASE

        // Handle app name text style toggle changes
        lowerCaseToggle.setOnCheckedChangeListener { _, isChecked ->
            val newStyle = if (isChecked) AppNameTextStyle.LEADING_UPPERCASE else AppNameTextStyle.ALL_LOWERCASE
            launcherPreferences.setAppNameTextStyle(newStyle)
            // Optionally, refresh the UI or notify the adapter if needed
        }

        setupFontSizeSelector()
        updateHiddenAppsList()
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
        // Set initial selection
        val currentSize = launcherPreferences.getFontSize()
        val buttonId = when (currentSize) {
            FontSize.SMALL -> R.id.sizeSmall
            FontSize.MEDIUM -> R.id.sizeMedium
            FontSize.LARGE -> R.id.sizeLarge
            FontSize.XLARGE -> R.id.sizeXLarge
        }
        fontSizeGroup.check(buttonId)

        // Handle selection changes
        fontSizeGroup.setOnCheckedChangeListener { _, checkedId ->
            val newSize = when (checkedId) {
                R.id.sizeSmall -> FontSize.SMALL
                R.id.sizeMedium -> FontSize.MEDIUM
                R.id.sizeLarge -> FontSize.LARGE
                R.id.sizeXLarge -> FontSize.XLARGE
                else -> FontSize.MEDIUM
            }
            launcherPreferences.setFontSize(newSize)
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    fun showAppOptions(
        context: Context,
        app: LauncherItem.App,
        view: View,
        launcherPreferences: LauncherPreferences,
        updateList: () -> Unit
    ) {
        PopupMenu(context, view).apply {
            // Check if the app is a favorite
            if (launcherPreferences.getFavorites().contains(app.appInfo.packageName)) {
                menu.add("Remove from Favorites")
            } else {
                menu.add("Add to Favorites")
            }

            // Check if the app is hidden
            if (app.appInfo.state == AppState.HIDDEN) {
                menu.add("Unhide App")
            } else {
                menu.add("Hide App")
            }

            menu.add("Rename")
            menu.add("App Info")

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.title.toString()) {
                    "Add to Favorites" -> {
                        launcherPreferences.addFavorite(app.appInfo.packageName)
                        Toast.makeText(context, "${app.appInfo.displayName()} added to favorites", Toast.LENGTH_SHORT).show()
                        updateList()
                        true
                    }
                    "Remove from Favorites" -> {
                        launcherPreferences.removeFavorite(app.appInfo.packageName)
                        Toast.makeText(context, "${app.appInfo.displayName()} removed from favorites", Toast.LENGTH_SHORT).show()
                        updateList()
                        true
                    }
                    "Rename" -> {
                        showRenameDialog(context, app, launcherPreferences, updateList)
                        true
                    }
                    "Hide App" -> {
                        launcherPreferences.hideApp(app.appInfo.packageName)
                        Toast.makeText(context, "${app.appInfo.displayName()} hidden", Toast.LENGTH_SHORT).show()
                        updateList()
                        true
                    }
                    "Unhide App" -> {
                        launcherPreferences.unhideApp(app.appInfo.packageName)
                        Toast.makeText(context, "${app.appInfo.displayName()} is now visible", Toast.LENGTH_SHORT).show()
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
            show()
        }
    }

    private fun openAppInfo(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        context.startActivity(intent)
    }

    private fun showRenameDialog(context: Context, app: LauncherItem.App, launcherPreferences: LauncherPreferences, updateList: () -> Unit) {
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
        // Retrieve all installed apps without filtering
        val allApps = AppUtils.getInstalledApps(this, launcherPreferences)
            .map { LauncherItem.App(it) }

        hiddenAppsView.adapter = AppAdapter(
            items = allApps,
            onItemClick = { /* Do nothing */ },
            onItemLongClick = { item, view ->
                if (item is LauncherItem.App) {
                    showAppOptions(
                        context = this,
                        app = item,
                        view = view,
                        launcherPreferences = launcherPreferences,
                        updateList = { updateHiddenAppsList() } // Refresh the list after changes
                    )
                    true
                } else false
            },
            isFavoritesList = false,
            fontSize = launcherPreferences.getFontSize(),
            launcherPreferences = launcherPreferences,
            showAppState = true // Show app state in this list
        )
    }


}