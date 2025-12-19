package com.example.shopping.ui.payment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.databinding.ItemOrderDetailBinding
import com.example.shopping.ui.user.order.OrderItem

class PaymentItemAdapter(
    private val items: List<OrderItem>
) : RecyclerView.Adapter<PaymentItemAdapter.VH>() {

    inner class VH(val binding: ItemOrderDetailBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOrderDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.binding.apply {
            val resId = holder.itemView.context.resources.getIdentifier(
                item.imageKey,
                "drawable",
                holder.itemView.context.packageName
            )
            if (resId != 0) {
                imgDetail.setImageResource(resId)
            } else {
                imgDetail.setImageResource(
                    com.example.shopping.R.drawable.ggicon_1
                )
            }
            txtDetailName.text = item.productName
            txtDetailSize.text = "尺寸： ${item.size}"
            txtDetailQty.text = "數量： ${item.quantity}"
            txtDetailPrice.text = "總共: NT$${item.price}"
        }
    }

    override fun getItemCount() = items.size
}
