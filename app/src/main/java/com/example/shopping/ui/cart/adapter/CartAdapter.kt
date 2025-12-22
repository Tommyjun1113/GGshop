package com.example.shopping.ui.cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.databinding.ItemCartBinding
import com.example.shopping.ui.cart.CartItem
import com.example.shopping.ui.cart.CartItemUI

class CartAdapter(
    private var items: List<CartItemUI>,
    private val listener: CartActions
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    interface CartActions {
        fun increase(item: CartItemUI)
        fun decrease(item: CartItemUI)
        fun delete(item: CartItemUI)
        fun onItemClick(item: CartItemUI)
        fun onSelectionChanged(item: CartItemUI, checked: Boolean)
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

            itemImage.setImageResource(item.imageRes)
            itemName.text = item.productName
            itemSize.text = "尺寸：${item.size}"
            itemPrice.text = "單價：NT$${item.price}"
            itemQty.text = item.quantity.toString()
            itemSubtotal.text = "小計：NT$${item.subtotal}"

            root.setOnClickListener { listener.onItemClick(item) }
            btnIncrease.setOnClickListener { listener.increase(item) }
            btnDecrease.setOnClickListener { listener.decrease(item) }
            checkSelect.setOnCheckedChangeListener(null)
            checkSelect.isChecked = item.isSelected

            checkSelect.setOnCheckedChangeListener { _, isChecked ->
                item.isSelected = isChecked
                listener.onSelectionChanged(item, isChecked)
            }

        }
    }

    override fun getItemCount() = items.size
    fun updateList(newList: List<CartItemUI>?) {
        if (newList != null) {
            items = newList
        }
        notifyDataSetChanged()
    }
}
