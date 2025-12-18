package com.example.shopping.ui.notifications.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.R
import com.example.shopping.ui.notifications.NotificationItem

class NotificationAdapter(
    private var list: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txt_title)
        val message: TextView = view.findViewById(R.id.txt_message)
        val expire: TextView = view.findViewById(R.id.txt_expire)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.title
        holder.message.text = item.message
        holder.expire.text = "有效期限：${item.expireDate}"

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = list.size
    fun updateList(newList: List<NotificationItem>) {
        list = newList
        notifyDataSetChanged()
    }
}

