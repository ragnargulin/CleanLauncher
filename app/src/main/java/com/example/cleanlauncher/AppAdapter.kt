package com.example.cleanlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val items: List<LauncherItem>,
    private val onItemClick: (LauncherItem) -> Unit,
    private val onItemLongClick: (LauncherItem, View) -> Boolean
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.apply {
            textSize = 24f
            setPadding(16, 24, 16, 24)
        }

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
                holder.textView.text = ""
                holder.itemView.setOnClickListener(null)
                holder.itemView.setOnLongClickListener(null)
            }
            is LauncherItem.App -> {
                holder.itemView.layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
                holder.textView.text = item.appInfo.name
                holder.itemView.setOnClickListener { onItemClick(item) }
                holder.itemView.setOnLongClickListener { view -> onItemLongClick(item, view) }
            }
            LauncherItem.AllApps -> {
                holder.itemView.layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
                holder.textView.text = "All Apps"
                holder.itemView.setOnClickListener { onItemClick(item) }
                holder.itemView.setOnLongClickListener(null)
            }
        }
    }

    override fun getItemCount() = items.size
}