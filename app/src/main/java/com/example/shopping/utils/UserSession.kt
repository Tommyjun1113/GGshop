package com.example.shopping.utils

import com.example.shopping.ui.notifications.Coupon



object UserSession {
        var isLogin: Boolean = false
        var email :String =""
        var documentId :String=""
        val favoriteCache = mutableSetOf<String>()
        var selectedCoupon : Coupon? = null
        var isManualCoupon : Boolean = false
        var isCouponEnabled: Boolean = false
        var hasAutoAppliedCoupon: Boolean = false


        var needMoreAmount: Int = 0
        var isRecommendForCoupon: Boolean = false


        var pendingSelectCartId: String? = null

        var preservedSelectedCartIds: MutableSet<String> = mutableSetOf()

        fun clear() {
                isLogin = false
                email = ""
                documentId = ""

                selectedCoupon = null
                isManualCoupon = false
                isCouponEnabled = false
                hasAutoAppliedCoupon = false
                needMoreAmount = 0
                isRecommendForCoupon = false
        }
}
