package com.example.shopping.ui.notifications


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val expireDate: String,
    val type: CouponType,
    val value: Int,
    val minSpend: Int = 0,
    val status: CouponStatus
) : Parcelable
