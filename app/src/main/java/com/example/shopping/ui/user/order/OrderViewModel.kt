package com.example.shopping.ui.user.order

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shopping.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore

class OrderDetailViewModel : ViewModel() {

    val items = MutableLiveData<List<OrderItem>>()
    val total = MutableLiveData<Int>()
    val discount = MutableLiveData<Int>()
    val couponTitle = MutableLiveData<String?>()
    val paymentMethod = MutableLiveData<String>()
    val createdAt = MutableLiveData<Long>()

    fun loadOrderDetail(orderId: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(UserSession.documentId)
            .collection("orders")
            .document(orderId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val orderItems = (doc.get("items") as? List<Map<String, Any>>)
                    ?.map { map ->
                        OrderItem(
                            productId = map["productId"].toString(),
                            productName = map["productName"].toString(),
                            price = (map["price"] as Number).toInt(),
                            size = map["size"].toString(),
                            quantity = (map["quantity"] as Number).toInt(),
                            imageKey = map["imageKey"].toString()
                        )
                    } ?: emptyList()

                items.value = orderItems
                total.value = (doc.getLong("total") ?: 0).toInt()
                discount.value = (doc.getLong("discount") ?: 0).toInt()
                val couponMap = doc.get("coupon") as? Map<*, *>
                couponTitle.value = couponMap?.get("title") as? String
                paymentMethod.value = doc.getString("paymentMethod") ?: ""
                createdAt.value = doc.getLong("createdAt") ?: 0L
            }
    }
}
