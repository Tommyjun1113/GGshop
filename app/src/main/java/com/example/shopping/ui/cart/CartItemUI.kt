package com.example.shopping.ui.cart

data class CartItemUI(
    val id: String,
    val productId: String,
    val productName: String,
    val imageKey: String = "",
    val imageRes: Int,
    val size: String,
    val price: Int,
    val quantity: Int,
    var isSelected: Boolean = false
) {
    val subtotal: Int
        get() = price * quantity
}