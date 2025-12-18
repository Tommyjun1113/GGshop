package com.example.shopping.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shopping.ui.notifications.CouponType
import com.example.shopping.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore

class CartViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val selectedIds = mutableSetOf<String>()
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems
    private val _discount = MutableLiveData(0)
    val discount: LiveData<Int> = _discount

    private val _total = MutableLiveData<Int>(0)
    val total: LiveData<Int> = _total

    fun loadCart() {
        if (!UserSession.isLogin) {
            _cartItems.value = emptyList()
            _discount.value = 0
            _total.value = 0
            return
        }

        db.collection("users")
            .document(UserSession.documentId)
            .collection("cart")
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { doc ->
                    CartItem(
                        id = doc.id,
                        productId = doc.getString("productId") ?: "",
                        productName = doc.getString("productName") ?: "",
                        price = (doc.getLong("price") ?: 0).toInt(),
                        size = doc.getString("size") ?: "",
                        quantity = (doc.getLong("quantity") ?: 1).toInt(),
                        imageResId = (doc.getLong("imageResId") ?: 0L).toInt(),
                        isSelected = selectedIds.contains(doc.id)
                    )
                }
                _cartItems.value = list
                calculateTotal(list)
            }
    }

    fun updateQty(item: CartItem, newQty: Int) {
        db.collection("users")
            .document(UserSession.documentId)
            .collection("cart")
            .document(item.id)
            .update("quantity", newQty)
            .addOnSuccessListener {
                updateItem(item.id) { it.copy(quantity = newQty) }
            }
    }

    fun updateSelection(item: CartItem, checked: Boolean) {
        if (checked) selectedIds.add((item.id)) else selectedIds.remove(item.id)

        updateItem(item.id) { it.copy(isSelected = checked) }
    }

    fun toggleSelectAll(checked: Boolean) {
        val list = _cartItems.value
            ?.map { if (checked) selectedIds.add(it.id) else selectedIds.clear()
            it.copy(isSelected = checked)
            }
            ?:return

        _cartItems.value = list
        calculateTotal(list)
    }

    fun deleteItem(item: CartItem) {
        db.collection("users")
            .document(UserSession.documentId)
            .collection("cart")
            .document(item.id)
            .delete()
            .addOnSuccessListener {
                val list = _cartItems.value
                    ?.filter { it.id != item.id }
                    ?: emptyList()

                _cartItems.value = list
                calculateTotal(list )
            }
    }

    fun deleteSelectedItems() {
        val list = _cartItems.value ?: return
        val selected = list.filter { it.isSelected }
        if (selected.isEmpty()) return

        val batch = db.batch()
        selected.forEach {
            val ref = db.collection("users")
                .document(UserSession.documentId)
                .collection("cart")
                .document(it.id)
            batch.delete(ref)
        }

        batch.commit().addOnSuccessListener {
            val newList = list.filterNot { it.isSelected }
            _cartItems.value = newList
            calculateTotal(newList)
        }
    }
    private fun updateItem(
        id: String,
        transform: (CartItem) -> CartItem
    ) {
        val list = _cartItems.value ?: return

        val newList = list.map {
            if (it.id == id) transform(it) else it
        }
        _cartItems.value = newList
        calculateTotal(newList)
    }

    private fun calculateTotal(list: List<CartItem>) {
        val selectedItems = list.filter { it.isSelected }
        val originalTotal = selectedItems.sumOf { it.subtotal }

        val coupon = UserSession.selectedCoupon
        var discountAmount =0

        if (coupon != null && originalTotal >= coupon.minSpend){
            discountAmount = when(coupon.type){
                CouponType.AMOUNT -> coupon.value
                CouponType.PERCENT -> (originalTotal * coupon.value / 100)
            }
        }

        _discount.value = discountAmount
        _total.value = originalTotal - discountAmount
    }
}
