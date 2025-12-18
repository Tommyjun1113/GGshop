package com.example.shopping.ui.notifications


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationItem(
    val coupon: Coupon,
    val message: String,
    val status: CouponStatus
) : Parcelable

