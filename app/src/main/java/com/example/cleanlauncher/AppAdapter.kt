package com.example.cleanlauncher

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppAdapter(
    var items: List<LauncherItem>,
    private val onItemClick: (LauncherItem) -> Unit,
    private val onItemLongClick: (LauncherItem, View) -> Boolean,
    private val isFavoritesList: Boolean = false,
    private val fontSize: FontSize = FontSize.MEDIUM,
    private val launcherPreferences: LauncherPreferences,
    private val showAppState: Boolean = false
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appNameView: TextView = view.findViewById(R.id.app_name)
        val appStateView: TextView = view.findViewById(R.id.app_state)
        val timeView: TextView = view.findViewById(R.id.time_display)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = items[position]) {
            is LauncherItem.App -> bindAppItem(holder, item)
            LauncherItem.AllApps -> bindAllAppsItem(holder)
        }
    }

    private fun bindAppItem(holder: ViewHolder, item: LauncherItem.App) {
        holder.itemView.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )

        val stateSymbol = when (item.appInfo.state) {
            AppState.FAVORITE -> "\u2764" // Unicode for heart
            AppState.HIDDEN -> "\uD83D\uDC7B" // Unicode for ghost
            AppState.NEITHER -> "" // No symbol for NEITHER
            AppState.BAD -> "\uD83D\uDC80" // Unicode for bad
        }

        if (showAppState) {
            holder.appStateView.text = stateSymbol
            holder.appStateView.visibility = if (stateSymbol.isNotEmpty()) View.VISIBLE else View.GONE
        } else {
            holder.appStateView.visibility = View.GONE
        }

        val textStyle = launcherPreferences.getAppNameTextStyle()
        val appName = item.appInfo.displayName()

        holder.appNameView.text = when (textStyle) {
            AppNameTextStyle.ALL_LOWERCASE -> appName.lowercase(Locale.getDefault())
            AppNameTextStyle.LEADING_UPPERCASE -> appName.replaceFirstChar { it.uppercase(Locale.getDefault()) }
        }

        holder.appNameView.textSize = fontSize.textSize

        Log.d("AppAdapter", "App: ${item.appInfo.name}, State: ${item.appInfo.state}")

        // Set text color only for BAD apps
        if (item.appInfo.state == AppState.BAD) {
            holder.appNameView.setTextColor(holder.itemView.context.getColor(R.color.grey))
            Log.d("AppAdapter", "Applied grey color to BAD app: ${item.appInfo.name}")
        }


        if (isFavoritesList && (item.appInfo.packageName == "com.android.deskclock" ||
                    item.appInfo.packageName == "com.google.android.deskclock")) {
            holder.timeView.text = timeFormat.format(Date())
            holder.timeView.textSize = fontSize.textSize
            holder.timeView.visibility = View.VISIBLE
        } else {
            holder.timeView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.itemView.setOnLongClickListener { view -> onItemLongClick(item, view) }
    }

    private fun bindAllAppsItem(holder: ViewHolder) {
        holder.itemView.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )

        holder.appNameView.text = holder.itemView.context.getString(R.string.all_apps)
        holder.appNameView.textSize = fontSize.textSize
        holder.timeView.visibility = View.GONE
        holder.itemView.setOnClickListener { onItemClick(LauncherItem.AllApps) }
        holder.itemView.setOnLongClickListener(null)
    }

    override fun getItemCount() = items.size
}