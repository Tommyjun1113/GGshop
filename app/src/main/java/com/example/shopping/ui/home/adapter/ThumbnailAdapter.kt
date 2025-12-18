package com.example.shopping.ui.home.adapter

import android.R.attr.onClick
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopping.R

class ThumbnailAdapter(
    private val images: List<Int> ,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ThumbnailAdapter.ThumbViewHolder>() {

    private var selectedIndex = 0

    inner class ThumbViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbViewHolder {
        val img = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_thumbnail, parent, false)
        return ThumbViewHolder(img)
    }

    override fun onBindViewHolder(
        holder: ThumbViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val resId = images[position]



        holder.img.setImageResource(resId)
        holder.img.alpha = if (position == selectedIndex) 1f else 0.4f

        holder.itemView.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()

            onClick(resId)
        }
    }

    override fun getItemCount() = images.size
}
