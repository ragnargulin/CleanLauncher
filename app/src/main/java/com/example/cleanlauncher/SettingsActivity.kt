package com.example.cleanlauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.RadioGroup
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var launcherPreferences: LauncherPreferences
    private lateinit var hiddenAppsView: RecyclerView
    private lateinit var hiddenAppsHeader: TextView
    private lateinit var togglersHeader: TextView
    private lateinit var togglersContainer: LinearLayout
    private lateinit var fontSizeHeader: TextView
    private lateinit var fontSizeGroup: RadioGroup
    private lateinit var statusBarToggle: SwitchMaterial
    private lateinit var themeToggle: SwitchMaterial
    private lateinit var lowerCaseToggle: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()
        setupInitialStates()
        setupListeners()
        updateHiddenAppsList()
    }

    private fun initializeViews() {
        launcherPreferences = LauncherPreferences(this)
        hiddenAppsView = findViewById(R.id.hidden_apps)
        hiddenAppsHeader = findViewById(R.id.all_apps_header)
        fontSizeHeader = findViewById(R.id.font_size_header)
        fontSizeGroup = findViewById(R.id.fontSizeGroup)
        statusBarToggle = findViewById(R.id.statusBarToggle)
        themeToggle = findViewById(R.id.themeToggle)
        lowerCaseToggle = findViewById(R.id.lowerCaseToggle)
        togglersHeader = findViewById(R.id.toggles_header)
        togglersContainer = findViewById(R.id.toggles_container)

        hiddenAppsView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupInitialStates() {
        statusBarToggle.isChecked = launcherPreferences.isStatusBarVisible()
        setStatusBarVisibility(statusBarToggle.isChecked)

        themeToggle.isChecked = launcherPreferences.isDarkMode()

        lowerCaseToggle.isChecked = launcherPreferences.getAppNameTextStyle() == AppNameTextStyle.LEADING_UPPERCASE

        setupFontSizeSelector()
    }

    private fun setupListeners() {
        statusBarToggle.setOnCheckedChangeListener { _, isChecked ->
            launcherPreferences.setStatusBarVisible(isChecked)
            setStatusBarVisibility(isChecked)
        }

        themeToggle.setOnCheckedChangeListener { _, isChecked ->
            launcherPreferences.toggleTheme()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate()
        }

        lowerCaseToggle.setOnCheckedChangeListener { _, isChecked ->
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
        fontSizeGroup.check(buttonId)

        fontSizeGroup.setOnCheckedChangeListener { _, checkedId ->
            val newSize = when (checkedId) {
                R.id.sizeSmall -> FontSize.SMALL
                R.id.sizeMedium -> FontSize.MEDIUM
                R.id.sizeLarge -> FontSize.LARGE
                R.id.sizeXLarge -> FontSize.XLARGE
                else -> FontSize.MEDIUM
            }
            launcherPreferences.setFontSize(newSize)
            restartMainActivity()
        }
    }

    private fun setupCollapsibleSections() {
        fontSizeHeader.setOnClickListener {
            toggleVisibility(fontSizeGroup)
        }

        togglersHeader.setOnClickListener {
            toggleVisibility(togglersContainer)
        }

        hiddenAppsHeader.setOnClickListener {
            toggleVisibility(hiddenAppsView)
        }
    }

    private fun toggleVisibility(view: View) {
        view.visibility = if (view.isVisible) View.GONE else View.VISIBLE
    }

    private fun restartMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
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