package com.example.shopping.ui.cart

data class CartItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val price: Int = 0,
    val size: String = "",
    var quantity: Int = 1,
    val imageKey: String,
    var isSelected: Boolean = false
) {
    val subtotal: Int get() = price * quantity
}