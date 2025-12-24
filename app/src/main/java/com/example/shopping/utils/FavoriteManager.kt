package com.example.shopping.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object FavoriteManager {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null
    private fun favoriteRef(uid: String, productId: String) =
        db.collection("users")
            .document(uid)
            .collection("favorites")
            .document(productId)

    fun startFavoriteSync(onUpdate: () -> Unit) {
        if (!UserSession.isLogin) return

        stopFavoriteSync()

        listener = db.collection("users")
            .document(UserSession.documentId)
            .collection("favorites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                UserSession.favoriteCache.clear()
                snapshot?.documents?.forEach {
                    UserSession.favoriteCache.add(it.id)
                }

                onUpdate()
            }
    }

    fun stopFavoriteSync() {
        listener?.remove()
        listener = null
    }

    fun isFavorite(productId: String): Boolean {
        return UserSession.favoriteCache.contains(productId)
    }

    fun toggleFavorite(productId: String) {
        val uid = UserSession.documentId
        val ref = db.collection("users")
            .document(uid)
            .collection("favorites")
            .document(productId)

        if (UserSession.favoriteCache.contains(productId)) {
            ref.delete()
        } else {
            ref.set(
                mapOf(
                    "productId" to productId,
                    "createdAt" to System.currentTimeMillis()
                )
            )
        }
    }
}
