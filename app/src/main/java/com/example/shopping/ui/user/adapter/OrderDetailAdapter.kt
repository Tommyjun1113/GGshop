package com.example.shopping.ui.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopping.R
import com.example.shopping.databinding.ItemOrderDetailBinding
import com.example.shopping.ui.user.order.OrderItem

class OrderDetailAdapter(
    private val items: List<OrderItem>,
) : RecyclerView.Adapter<OrderDetailAdapter.DetailViewHolder>() {

    inner class DetailViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemOrderDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply{

            val imageResId = root.context.resources.getIdentifier(
                item.imageKey,
                "drawable",
                root.context.packageName
            )

            if (imageResId != 0) {
                imgDetail.setImageResource(imageResId)
            } else {
                imgDetail.setImageResource(R.drawable.ggicon_1)
            }
            txtDetailName.text = item.productName
            txtDetailSize.text = "尺寸：${item.size}"
            txtDetailQty.text = "數量：${item.quantity}"
            txtDetailPrice.text = "單價：NT$${item.price}"
        }
    }

    override fun getItemCount() = items.size
}
