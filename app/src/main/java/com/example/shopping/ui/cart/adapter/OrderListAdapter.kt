package com.example.shopping.ui.cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.databinding.ItemOrderBinding
import com.example.shopping.ui.cart.order.OrderModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderListAdapter(private val items: List<OrderModel>) :
    RecyclerView.Adapter<OrderListAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = items[position]

        val date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            .format(Date(item.createdAt))

        holder.binding.apply {
            txtOrderTotal.text = "總金額：NT$${item.total}"
            txtOrderTime.text = date
        }
    }

    override fun getItemCount() = items.size
}