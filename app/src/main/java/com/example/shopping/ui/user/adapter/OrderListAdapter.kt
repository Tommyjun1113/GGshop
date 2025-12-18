package com.example.shopping.ui.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.databinding.ItemOrderBinding
import com.example.shopping.ui.user.order.OrderModel

class OrderListAdapter(
    private val orders: List<OrderModel>,
    private val onClick: (OrderModel) -> Unit
) : RecyclerView.Adapter<OrderListAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        val firstItem = order.items.firstOrNull()


        holder.binding.apply {
            imgOrderThumb.setImageResource(firstItem?.imageResId ?:0)
            txtOrderTitle.text = firstItem?.productName?:"商品"
            txtOrderTime.text = order.formattedTime
            txtPaymentMethod.text = "付款方式：${order.paymentMethod}"
            txtOrderTotal.text = "總金額：NT$${order.total}"

            root.setOnClickListener { onClick(order) }
        }
    }

    override fun getItemCount() = orders.size
}
