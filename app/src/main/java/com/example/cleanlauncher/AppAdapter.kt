package com.example.cleanlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppAdapter(
    val items: List<LauncherItem>,
    private val onItemClick: (LauncherItem) -> Unit,
    private val onItemLongClick: (LauncherItem, View) -> Boolean,
    private val isFavoritesList: Boolean = false
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appNameView: TextView = view.findViewById(R.id.app_name)
        val timeView: TextView = view.findViewById(R.id.time_display)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        when (item) {
            is LauncherItem.Spacer -> {
                holder.itemView.layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    holder.itemView.context.resources.displayMetrics.heightPixels / 2
                )
                holder.appNameView.text = ""
                holder.timeView.text = ""
                holder.itemView.setOnClickListener(null)
                holder.itemView.setOnLongClickListener(null)
            }
            is LauncherItem.App -> {
                holder.itemView.layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
                holder.appNameView.text = item.appInfo.name

                // Show time only for clock app in favorites list
                if (isFavoritesList && (item.appInfo.packageName == "com.android.deskclock" ||
                            item.appInfo.packageName == "com.google.android.deskclock")) {
                    holder.timeView.text = timeFormat.format(Date())
                    holder.timeView.visibility = View.VISIBLE
                } else {
                    holder.timeView.visibility = View.GONE
                }

                holder.itemView.setOnClickListener { onItemClick(item) }
                holder.itemView.setOnLongClickListener { view -> onItemLongClick(item, view) }
            }
            LauncherItem.AllApps -> {
                holder.itemView.layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
                holder.appNameView.text = holder.itemView.context.getString(R.string.all_apps)
                holder.timeView.visibility = View.GONE
                holder.itemView.setOnClickListener { onItemClick(item) }
                holder.itemView.setOnLongClickListener(null)
            }
        }
    }

    override fun getItemCount() = items.size
}