package com.example.shopping.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shopping.ui.home.ProductDataSource
import com.example.shopping.ui.notifications.Coupon
import com.example.shopping.ui.notifications.CouponType
import com.example.shopping.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.filter

class CartViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val selectedIds = mutableSetOf<String>()
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _cartItemsUI = MutableLiveData<List<CartItemUI>>()
    val cartItemsUI: LiveData<List<CartItemUI>> = _cartItemsUI

    private val _discount = MutableLiveData(0)
    val discount: LiveData<Int> = _discount

    private var userCoupons: List<Coupon> = emptyList()

    private val _total = MutableLiveData<Int>(0)
    val total: LiveData<Int> = _total

    private var pendingSelectId: String? = null
    fun loadCart() {
        if (!UserSession.isLogin) {
            _cartItems.value = emptyList()
            _cartItemsUI.value = emptyList()
            _discount.value = 0
            _total.value = 0
            return
        }

        loadUserCoupons {
            db.collection("users")
                .document(UserSession.documentId)
                .collection("cart")
                .get()
                .addOnSuccessListener { result ->
                    val list = result.mapNotNull { doc ->
                        val productId = doc.getString("productId") ?: return@mapNotNull null
                        val product = ProductDataSource.findById(productId) ?: return@mapNotNull null
                        CartItem(
                            id = doc.id,
                            productId = productId,
                            productName = product.name,
                            price = (doc.getLong("price") ?: 0).toInt(),
                            size = doc.getString("size") ?: "",
                            quantity = (doc.getLong("quantity") ?: 1).toInt(),
                            imageRes= product.imageResId.first(),
                            isSelected = selectedIds.contains(doc.id)
                        )
                    }.toMutableList()
                    pendingSelectId?.let { id ->
                        selectedIds.add(id)
                        val index = list.indexOfFirst { it.id == id }
                        if (index != -1) {
                            list[index] = list[index].copy(isSelected = true)
                        }
                        pendingSelectId = null
                    }
                    _cartItems.value = list
                    _cartItemsUI.value = mapToUI(list)
                    calculateTotal(list)
                }

        }
    }

    private fun mapToUI(list: List<CartItem>): List<CartItemUI> =
        list.map {
            CartItemUI(
                id = it.id,
                productId = it.productId,
                productName = it.productName,
                imageRes = it.imageRes,
                size = it.size,
                price = it.price,
                quantity = it.quantity,
                isSelected = it.isSelected
            )
        }

    private fun loadUserCoupons(onDone: () -> Unit) {
        db.collection("users")
            .document(UserSession.documentId)
            .collection("coupons")
            .get()
            .addOnSuccessListener { result ->
                userCoupons = result.mapNotNull { doc ->
                    try {
                        Coupon(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            type = CouponType.valueOf(doc.getString("type") ?: "AMOUNT"),
                            value = (doc.getLong("value") ?: 0).toInt(),
                            minSpend = (doc.getLong("minSpend") ?: 0).toInt(),
                            expireDate = doc.getString("expireDate") ?: "",
                            used = doc.getBoolean("used") ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onDone()
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
        if (checked) selectedIds.add(item.id) else selectedIds.remove(item.id)
        updateItem(item.id) { it.copy(isSelected = checked) }
    }

    fun selectItemById(id: String) {
        selectedIds.add(id)

        val list = _cartItems.value ?: return

        val newList = list.map {
            if (it.id == id) it.copy(isSelected = true)
            else it
        }

        _cartItems.value = newList
        _cartItemsUI.value = mapToUI(newList)
        calculateTotal(newList)
    }

    fun setPendingSelect(id: String) {
        pendingSelectId = id
    }

    fun toggleSelectAll(checked: Boolean) {
        val list = _cartItems.value ?: return

        selectedIds.clear()
        if (checked) list.forEach { selectedIds.add(it.id) }

        val newList = list.map { it.copy(isSelected = checked) }
        _cartItems.value = newList
        _cartItemsUI.value = mapToUI(newList)
        calculateTotal(newList)
    }

    fun deleteItem(item: CartItem) {
        db.collection("users")
            .document(UserSession.documentId)
            .collection("cart")
            .document(item.id)
            .delete()
            .addOnSuccessListener {
                val newList = _cartItems.value?.filter { it.id != item.id } ?: emptyList()
                selectedIds.remove(item.id)
                _cartItems.value = newList
                _cartItemsUI.value = mapToUI(newList)
                calculateTotal(newList)
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
            selectedIds.clear()
            _cartItems.value = newList
            _cartItemsUI.value = mapToUI(newList)
            calculateTotal(newList)
        }
    }

    private fun updateItem(id: String, transform: (CartItem) -> CartItem) {
        val list = _cartItems.value ?: return
        val newList = list.map { if (it.id == id) transform(it) else it }
        _cartItems.value = newList
        _cartItemsUI.value = mapToUI(newList)
        calculateTotal(newList)
    }



    fun calculateTotal(list: List<CartItem>) {
        val selectedItems = list.filter { it.isSelected }
        val originalTotal = selectedItems.sumOf { it.subtotal }

        if (originalTotal == 0) {
            _discount.value = 0
            _total.value = 0
            return
        }
        if (
            !UserSession.isManualCoupon &&
            !UserSession.hasAutoAppliedCoupon &&
            UserSession.selectedCoupon == null
        ) {
            autoApplyBestCoupon(userCoupons, originalTotal)
            UserSession.hasAutoAppliedCoupon = true
            UserSession.isCouponEnabled = UserSession.selectedCoupon != null
        }

        if (!UserSession.isCouponEnabled) {
            _discount.value = 0
            _total.value = originalTotal
            return
        }

        val coupon = UserSession.selectedCoupon

        if (coupon == null || originalTotal < coupon.minSpend) {
            _discount.value = 0
            _total.value = originalTotal


            if (coupon != null) {
                UserSession.needMoreAmount = coupon.minSpend - originalTotal
                UserSession.isRecommendForCoupon = true
            }
            return
        }

        val discountAmount = when (coupon.type) {
            CouponType.AMOUNT -> coupon.value
            CouponType.PERCENT -> originalTotal * coupon.value / 100
        }

        _discount.value = discountAmount
        _total.value = originalTotal - discountAmount
    }



    private fun autoApplyBestCoupon(coupons: List<Coupon>, total: Int) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

        val bestCoupon = coupons
            .filter { !it.used }
            .filter {
                val expire = LocalDate.parse(it.expireDate, formatter)
                !expire.isBefore(today)
            }
            .filter { total >= it.minSpend }
            .maxByOrNull { coupon ->
                when (coupon.type) {
                    CouponType.AMOUNT -> coupon.value
                    CouponType.PERCENT -> total * coupon.value / 100
                }
            }

        UserSession.selectedCoupon = bestCoupon
    }
}
