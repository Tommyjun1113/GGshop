package com.example.shopping.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NotificationsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _notifications = MutableLiveData<List<NotificationItem>>()
    val notifications: LiveData<List<NotificationItem>> = _notifications

    private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    fun loadNotifications(userId: String) {
        if (userId.isEmpty()) {
            _notifications.value = emptyList()
            return
        }

        db.collection("users")
            .document(userId)
            .collection("coupons")
            .get()
            .addOnSuccessListener { result ->

                val today = LocalDate.now()

                val list = result.mapNotNull { doc ->

                    val expireStr = doc.getString("expireDate") ?: return@mapNotNull null
                    val used = doc.getBoolean("used") ?: false

                    val expireDate = try {
                        LocalDate.parse(expireStr, formatter)
                    } catch (e: Exception) {
                        return@mapNotNull null
                    }

                    val (status, message) = when {
                        used -> CouponStatus.USED to "已使用"
                        expireDate.isBefore(today) -> CouponStatus.EXPIRED to "已過期"
                        else -> CouponStatus.AVAILABLE to "立即使用"
                    }

                    val coupon = Coupon(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        type = CouponType.valueOf(doc.getString("type") ?: "AMOUNT"),
                        value = (doc.getLong("value") ?: 0).toInt(),
                        minSpend = (doc.getLong("minSpend") ?: 0).toInt(),
                        expireDate = expireStr,
                        used = used
                    )

                    NotificationItem(
                        coupon = coupon,
                        message = message,
                        status = status
                    )

                }

                _notifications.value = list
            }
    }
}

