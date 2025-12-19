package com.example.shopping.ui.cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopping.R
import com.example.shopping.databinding.ItemCartBinding
import com.example.shopping.ui.cart.CartItem

class CartAdapter(
    private var items: List<CartItem>,
    private val listener: CartActions
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    interface CartActions {
        fun increase(item: CartItem)
        fun decrease(item: CartItem)
        fun delete(item: CartItem)
        fun onItemClick(item: CartItem)
        fun onSelectionChanged(item: CartItem, checked: Boolean)
    }

    inner class CartViewHolder(val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {


            val context = root.context
            val resId = context.resources.getIdentifier(
                item.imageKey,          // 👈 例如 "nike_1"
                "drawable",
                context.packageName
            )

            if (resId != 0) {
                itemImage.setImageResource(resId)
            } else {
                itemImage.setImageResource(R.drawable.ggicon_1)
            }
            itemName.text = item.productName
            itemSize.text = "尺寸：${item.size}"
            itemPrice.text = "單價：NT$${item.price}"
            itemQty.text = item.quantity.toString()
            itemSubtotal.text = "小計：NT$${item.subtotal}"

            root.setOnClickListener { listener.onItemClick(item) }
            btnIncrease.setOnClickListener { listener.increase(item) }
            btnDecrease.setOnClickListener { listener.decrease(item) }
            root.setOnClickListener {
                listener.onItemClick(item)
            }
            checkSelect.setOnCheckedChangeListener(null)
            checkSelect.isChecked = item.isSelected

            checkSelect.setOnCheckedChangeListener { _, isChecked ->
                item.isSelected = isChecked
                listener.onSelectionChanged(item, isChecked)
            }

        }
    }

    override fun getItemCount() = items.size
    fun updateList(newList: List<CartItem>) {
        items = newList
        notifyDataSetChanged()
    }
}
