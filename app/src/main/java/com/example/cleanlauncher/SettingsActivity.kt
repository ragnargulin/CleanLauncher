package com.example.cleanlauncher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.RadioGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class SettingsActivity : AppCompatActivity() {
    private lateinit var launcherPreferences: LauncherPreferences
    private lateinit var hiddenAppsView: RecyclerView
    private lateinit var fontSizeGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        launcherPreferences = LauncherPreferences(this)
        hiddenAppsView = findViewById(R.id.hidden_apps)
        hiddenAppsView.layoutManager = LinearLayoutManager(this)
        fontSizeGroup = findViewById(R.id.fontSizeGroup)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        setupFontSizeSelector()
        updateHiddenAppsList()
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