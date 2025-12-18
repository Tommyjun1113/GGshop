package com.example.shopping.ui.user.order

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OrderItem(
    val productId: String,
    val productName: String,
    val price: Int,
    val size: String,
    val quantity: Int,
    val imageResId: Int
) : Parcelable