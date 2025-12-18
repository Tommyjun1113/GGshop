package com.example.shopping.ui.notifications.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.R
import com.example.shopping.ui.notifications.CouponStatus
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

        holder.title.text = item.coupon.title
        holder.message.text = item.message
        holder.expire.text = "有效期限：${item.coupon.expireDate}"

        val gray = Color.parseColor("#9E9E9E")
        val black = Color.parseColor("#000000")
        val textColor = if (item.status == CouponStatus.AVAILABLE) black else gray
        holder.title.setTextColor(textColor)
        holder.message.setTextColor(textColor)
        holder.expire.setTextColor(textColor)

        when(item.status){
            CouponStatus.AVAILABLE -> {
                holder.itemView.alpha = 1f
                holder.itemView.isEnabled = true
                holder.itemView.setOnClickListener {
                    onItemClick(item)
                }
            }
            CouponStatus.USED , CouponStatus.EXPIRED ->{
                holder.itemView.alpha = 0.4f
                holder.itemView.isEnabled = false
                holder.itemView.setOnClickListener(null)
            }
        }
    }

    override fun getItemCount(): Int = list.size
    fun updateList(newList: List<NotificationItem>) {
        list = newList
        notifyDataSetChanged()
    }
}

