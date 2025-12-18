package com.example.shopping.ui.user.order

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.RawValue
@Parcelize
data class OrderModel(
    val id: String = "",
    val items: List<OrderItem>,
    val total: Int,
    val discount :Int = 0,
    val couponTitle: String? = null,
    val paymentMethod: String = "",
    val createdAt: Long = 0L,
    val fromCart: Boolean = true
) : Parcelable {
    val formattedTime: String
        get() {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.TAIWAN)
            return sdf.format(java.util.Date(createdAt))
        }
}
