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

                    val status = when {
                        used -> CouponStatus.USED
                        expireDate.isBefore(today) -> CouponStatus.EXPIRED
                        else -> CouponStatus.AVAILABLE
                    }

                    NotificationItem(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        message = "立即使用",
                        expireDate = expireStr,
                        type = CouponType.valueOf(doc.getString("type")!!),
                        value = (doc.getLong("value") ?: 0).toInt(),
                        minSpend = (doc.getLong("minSpend") ?: 0).toInt(),
                        status = status
                    )
                }

                _notifications.value = list
            }
    }
}

