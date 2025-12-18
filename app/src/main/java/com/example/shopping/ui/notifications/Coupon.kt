package com.example.shopping.ui.notifications

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Coupon(
    val id: String = "",
    val title: String = "",
    val type: CouponType = CouponType.AMOUNT,
    val value: Int = 0,
    val minSpend: Int = 0,
    val expireDate: String = "",
    val used: Boolean = false
): Parcelable
