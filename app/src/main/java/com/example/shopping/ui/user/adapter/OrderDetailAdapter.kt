package com.example.shopping.ui.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopping.R
import com.example.shopping.databinding.ItemOrderDetailBinding

class OrderDetailAdapter(
    private val items: List<Map<String, Any>>,
    private val paymentMethod: String
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

        with(holder.binding) {
            val imageResId= (item["imageResId"]as? Number)?.toInt()?:0
            if (imageResId != 0){
                imgDetail.setImageResource(imageResId)
            }else{
                imgDetail.setImageResource(null ?:0)
            }

            txtDetailName.text = item["productName"].toString() ?: ""
            txtDetailSize.text = "尺寸：${item["size"]}"
            txtDetailQty.text = "數量：${item["quantity"]}"
            txtDetailPrice.text = "單價：NT$${item["price"]}"
        }
    }

    override fun getItemCount() = items.size
}
