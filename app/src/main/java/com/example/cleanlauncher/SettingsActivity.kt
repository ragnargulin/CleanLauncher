package com.example.cleanlauncher

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        launcherPreferences = LauncherPreferences(this)
        hiddenAppsView = findViewById(R.id.hidden_apps)
        hiddenAppsView.layoutManager = LinearLayoutManager(this)
        fontSizeGroup = findViewById(R.id.fontSizeGroup)
        statusBarToggle = findViewById(R.id.statusBarToggle)
        themeToggle = findViewById(R.id.themeToggle)

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

    private fun updateHiddenAppsList() {
        val hiddenApps = AppUtils.getInstalledApps(this, launcherPreferences)
            .filter { it.state == AppState.HIDDEN }
            .map { LauncherItem.App(it) }

        hiddenAppsView.adapter = AppAdapter(
            items = hiddenApps,
            onItemClick = { /* Do nothing */ },
            onItemLongClick = { item, _ ->
                if (item is LauncherItem.App) {
                    showUnhideOption(item)
                    true
                } else false
            },
            isFavoritesList = false,
            fontSize = launcherPreferences.getFontSize()
        )
    }

    private fun showUnhideOption(app: LauncherItem.App) {
        AlertDialog.Builder(this)
            .setTitle("Unhide App")
            .setMessage("Do you want to unhide ${app.appInfo.displayName()}?")
            .setPositiveButton("Unhide") { _, _ ->
                launcherPreferences.unhideApp(app.appInfo.packageName)
                updateHiddenAppsList()
                Toast.makeText(this, "${app.appInfo.displayName()} is now visible", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}